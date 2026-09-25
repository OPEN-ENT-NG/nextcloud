package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.security.CanAccessNextcloud;
import fr.openent.nextcloud.security.OwnerFilter;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.openent.nextcloud.service.UserService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.CookieHelper;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;


public class UserController extends ControllerHelper {

    private final UserService userService;
    private final Map<String, NextcloudConfig> nextcloudConfigMapByHost;

    private static final String OAUTH_STATE_COOKIE = "nextcloud_oauth_state";
    private static final int OAUTH_STATE_TTL_SECONDS = 600;
    private static final String OAUTH_AUTHORIZE_ENDPOINT = "/apps/oauth2/authorize";

    public UserController(ServiceFactory serviceFactory) {
        this.userService = serviceFactory.userService();
        this.nextcloudConfigMapByHost = serviceFactory.nextcloudConfigMapByHost();
    }

    @Get("/user/:userid/provide/token")
    @ApiDoc("Provide nextcloud token")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(CanAccessNextcloud.class)
    public void provideUserSession(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            UserNextcloud.RequestBody userCreationBody = new UserNextcloud.RequestBody()
                    .setUserId(user.getUserId())
                    .setDisplayName(user.getUsername());
            userService.provideUserSession(Renders.getHost(request), userCreationBody)
                    .onSuccess(userNextcloud -> renderJson(request, new JsonObject().put(Field.STATUS, Field.OK)))
                    .onFailure(err -> renderError(request));
        });
    }

    @Get("/user/:userid")
    @ApiDoc("get user info view")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void getUserInfo(HttpServerRequest request) {
        String userId = request.getParam(Field.USERID);
        userService.getUserInfo(Renders.getHost(request), userId)
                .onSuccess(userNextcloud -> renderJson(request, userNextcloud.toJSON()))
                .onFailure(err -> renderError(request));
    }

    @Get("user/oauth2/init")
    @ApiDoc("Init oauth2 login flow")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void initOauth2LoginFlow(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user == null) {
                unauthorized(request);
                return;
            }
            final NextcloudConfig nextcloudConfig = this.nextcloudConfigMapByHost.get(Renders.getHost(request));
            final String state = UUID.randomUUID().toString();
            CookieHelper.getInstance().setSigned(OAUTH_STATE_COOKIE, state, OAUTH_STATE_TTL_SECONDS, request);
            final String host = nextcloudConfig.host().endsWith("/")
                    ? nextcloudConfig.host().substring(0, nextcloudConfig.host().length() - 1)
                    : nextcloudConfig.host();

            final String encodedOauthClientId = urlEncode( nextcloudConfig.oauthClientId());
            final String encodedOauthState = urlEncode(state);
            final String encodedRedirectUri = urlEncode(nextcloudConfig.oauthRedirectUri());

            System.out.println(encodedRedirectUri);
            final String authorizeUrl = host + OAUTH_AUTHORIZE_ENDPOINT
                    + "?" + Field.RESPONSE_TYPE + "=" + Field.CODE
                    + "&" + Field.OAUTH_CLIENT_ID + "=" + encodedOauthClientId
                    + "&" + Field.STATE + "=" + encodedOauthState
                    + "&" + Field.REDIRECT_URI + "=" + encodedRedirectUri;
            request.response().putHeader("Location", authorizeUrl).setStatusCode(302).end();
        });
    }

    @Get("user/oauth2/client")
    @ApiDoc("OAuth2 callback : exchange authorization code for a token and notify the opener")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void oauth2Callback(HttpServerRequest request) {
        final String code = request.getParam(Field.CODE);
        final String receivedState = request.getParam(Field.STATE);
        final String expectedState = CookieHelper.getInstance().getSigned(OAUTH_STATE_COOKIE, request);
        CookieHelper.getInstance().setSigned(OAUTH_STATE_COOKIE, "", 0, request);

        if (code == null || expectedState == null || !expectedState.equals(receivedState)) {
            renderOauthError(request);
            return;
        }

        UserUtils.getUserInfos(eb, request, user -> {
            if (user == null) {
                renderOauthError(request);
                return;
            }
            userService.exchangeAuthorizationCode(Renders.getHost(request), code)
                    .compose(token -> userService.persistOauthTokens(user.getUserId(), token))
                    .onSuccess(v -> renderOauthSuccess(request))
                    .onFailure(err -> renderOauthError(request));
        });
    }

    /**
     * Render the popup landing page notifying the opener of a successful connection.
     */
    private void renderOauthSuccess(HttpServerRequest request) {
        final String origin = Json.encode(Renders.getScheme(request) + "://" + Renders.getHost(request));
        final String html = "<!DOCTYPE html><html><body><script>"
                + "window.opener.postMessage({ type: 'nextcloud-connected' }, " + origin + ");"
                + "window.close();"
                + "</script></body></html>";
        request.response().putHeader("Content-Type", "text/html; charset=utf-8").end(html);
    }

    /**
     * Render an error landing page ; no postMessage is sent to the opener.
     */
    private void renderOauthError(HttpServerRequest request) {
        final String html = "<!DOCTYPE html><html><body>Nextcloud authorization failed.</body></html>";
        request.response().setStatusCode(400).putHeader("Content-Type", "text/html; charset=utf-8").end(html);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Get("/user/:userid/oauth2/status")
    @ApiDoc("Get Nextcloud OAuth2 connection status")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void oauth2Status(HttpServerRequest request) {
        String userId = request.getParam(Field.USERID);
        userService.getOauthStatus(userId)
                .onSuccess(connected -> renderJson(request, new JsonObject().put(Field.CONNECTED, connected)))
                .onFailure(err -> renderError(request));
    }

}
