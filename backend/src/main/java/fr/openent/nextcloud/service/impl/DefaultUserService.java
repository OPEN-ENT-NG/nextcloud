package fr.openent.nextcloud.service.impl;

import fr.openent.nextcloud.Nextcloud;
import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.core.enums.EditableDataField;
import fr.openent.nextcloud.helper.HttpResponseHelper;
import fr.openent.nextcloud.helper.PromiseHelper;
import fr.openent.nextcloud.model.OCSResponse;
import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.openent.nextcloud.service.TokenProviderService;
import fr.openent.nextcloud.service.UserService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.apache.commons.text.RandomStringGenerator;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DefaultUserService implements UserService {
    private final Logger log = LoggerFactory.getLogger(DefaultUserService.class);
    private final WebClient client;
    private final TokenProviderService tokenProviderService;
    private final DocumentsService documentsService;
    private final Map<String, NextcloudConfig> nextcloudConfigMapByHost;

    private static final String USER_ENDPOINT = "/cloud/users";

    public DefaultUserService(ServiceFactory serviceFactory) {
        this.client = serviceFactory.webClient();
        this.tokenProviderService = serviceFactory.tokenProviderService();
        this.documentsService = serviceFactory.documentsService();
        this.nextcloudConfigMapByHost = serviceFactory.nextcloudConfigMapByHost();
    }

    @Override
    public Future<JsonObject> provideUserSession(final String host, UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        this.getUserInfo(host, userBody.userId())
                .compose(userNextcloud -> resolveUserSession(host, userBody, userNextcloud))
                .onSuccess(res -> promise.complete())
                .onFailure(promise::fail);
        return promise.future();
    }

    /**
     * This method will attempt to resolve user's session by providing its token
     * This will be proceeded in several steps :
     *  - With fetched user nextcloud info, we determine whether its id is existent and if its session (sql) is already created
     *  in that case we do nothing
     *  - However, if its missing session we create a new session by changing its password and creating a new token session
     *
     *  If none is existent, we create a new user and we provide its session
     *
     * @param host host
     * @param   userBody        User Body request {@link UserNextcloud.RequestBody}
     * @param   userNextcloud   user nextcloud info {@link UserNextcloud}
     * @return  Empty response (succeed will resolve user's session by persisting it via database)
     */
    private Future<Void> resolveUserSession(final String host, UserNextcloud.RequestBody userBody, UserNextcloud userNextcloud) {
        Promise<Void> promise = Promise.promise();
        if (userNextcloud.id() != null) {
            this.getUserSession(userBody.userId())
                    .compose(userSession -> this.checkSessionValidity(host, userSession))
                    .onSuccess(userSession -> {
                        if (userSession.isEmpty()) {
                            userBody.setPassword(generateUserPassword());
                            this.changeUserPassword(host, userBody)
                                    .compose(res -> this.tokenProviderService.provideNextcloudSession(host, userBody))
                                    .onSuccess(res -> promise.complete())
                                    .onFailure(promise::fail);
                        } else {
                            promise.complete();
                        }
                    })
                    .onFailure(promise::fail);
        } else {
            // case no exist, we create a user and its session token
            userBody.setPassword(generateUserPassword());
            this.addNewUser(host, userBody)
                    .compose(res -> this.tokenProviderService.provideNextcloudSession(host, userBody))
                    .onSuccess(res -> promise.complete())
                    .onFailure(promise::fail);
        }
        return promise.future();
    }

    /**
     * Check if the user's token is still up-to-date in the database
     * @param host host
     * @param userSession   User session
     * @return              Future with the updated session if update needed, else old session.
     */
    private Future<UserNextcloud.TokenProvider> checkSessionValidity(String host, UserNextcloud.TokenProvider userSession) {
        Promise<UserNextcloud.TokenProvider> promise = Promise.promise();
        if (userSession.isEmpty()) {
            promise.complete(new UserNextcloud.TokenProvider());
        } else {
            documentsService.parameterizedListFiles(host, userSession, null, response -> {
                if (response.failed()) {
                    String messageToFormat = "[Nextcloud@%s::checkSessionValidity] Error during request for token validity check : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response.cause(), promise);
                } else {
                    handleSessionCheckStatus(userSession, response, promise);
                }
            });
        }
        return promise.future();
    }

    /**
     * Complete the promise depending on the status code of the token check request.
     * @param userSession   User session.
     * @param response      Check token request (can be anything, just to check user access to his nc account).
     * @param promise       Promise to complete.
     */
    private void handleSessionCheckStatus(UserNextcloud.TokenProvider userSession, AsyncResult<HttpResponse<String>> response, Promise<UserNextcloud.TokenProvider> promise) {
        int statusCode = response.result().statusCode();
        if (statusCode == 401) {
            promise.complete(new UserNextcloud.TokenProvider());
        } else if (statusCode == 200 || statusCode == 207) {
            promise.complete(userSession);
        } else {
            String messageToFormat = "[Nextcloud@%s::handleSessionCheckStatus] Request responded error code : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response.cause(), promise);
        }
    }

    private Future<JsonObject> addNewUser(final String host, UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        final NextcloudConfig nextcloudConfig = this.nextcloudConfigMapByHost.get(host);
        this.client.postAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint() + USER_ENDPOINT)
                .basicAuthentication(nextcloudConfig.username(), nextcloudConfig.password())
                .putHeader(Field.OCS_API_REQUEST, String.valueOf(true))
                .addQueryParam(Field.FORMAT, Field.JSON)
                .as(BodyCodec.string(StandardCharsets.UTF_8.toString()))
                .sendJsonObject(userBody.toJSON(nextcloudConfig), responseAsync -> proceedUserCreation(nextcloudConfig, userBody, responseAsync, promise));
        return promise.future();
    }

    /**
     * Proceed async event after HTTP adding new user API endpoint has been sent
     *
     * @param   nextcloudConfig nextcloudConfig
     * @param   userBody        request user body sent {@link UserNextcloud.RequestBody}
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link JsonObject}
     */
    private void proceedUserCreation(final NextcloudConfig nextcloudConfig, UserNextcloud.RequestBody userBody, AsyncResult<HttpResponse<String>> responseAsync,
                                     Promise<JsonObject> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::proceedUserCreation] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<String> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String messageToFormat = "[Nextcloud@%s::proceedUserCreation] Response status is not a HTTP 200 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                JsonObject result = new JsonObject()
                        .put(Field.STATUS, Field.OK)
                        .put(Field.DATA, userBody.toJSON(nextcloudConfig));
                promise.complete(result);
            }
        }
    }

    @Override
    public Future<UserNextcloud> getUserInfo(String host, String userId) {
        Promise<UserNextcloud> promise = Promise.promise();
        final NextcloudConfig nextcloudConfig = this.nextcloudConfigMapByHost.get(host);
        this.client.getAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint() + USER_ENDPOINT + "/" + userId)
                .basicAuthentication(nextcloudConfig.username(), nextcloudConfig.password())
                .putHeader(Field.OCS_API_REQUEST, String.valueOf(true))
                .as(BodyCodec.jsonObject())
                .addQueryParam(Field.FORMAT, Field.JSON)
                .send(responseAsync -> proceedUserInfo(responseAsync, promise));
        return promise.future();
    }

    /**
     * Proceed async event after HTTP get user API endpoint has been sent
     *
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link UserNextcloud}
     */
    private void proceedUserInfo(AsyncResult<HttpResponse<JsonObject>> responseAsync, Promise<UserNextcloud> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::getUserInfo] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<JsonObject> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String messageToFormat = "[Nextcloud@%s::getUserInfo] Response status is not a HTTP 200 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                OCSResponse ocsResponse = new OCSResponse(response.body().getJsonObject(Field.OCS, new JsonObject()));
                promise.complete(new UserNextcloud(ocsResponse.data()));
            }
        }
    }

    @Override
    public Future<UserNextcloud.TokenProvider> getUserSession(String userId) {
        Promise<UserNextcloud.TokenProvider> promise = Promise.promise();
        String query = "SELECT * FROM " + Nextcloud.DB_SCHEMA + ".user WHERE user_id = ?";
        JsonArray param = new JsonArray().add(userId);
        Sql.getInstance().prepared(query, param, SqlResult.validUniqueResultHandler(event -> {
            if (event.isLeft()) {
                String messageToFormat = "[Nextcloud@%s::getUserSession] An error has occurred during fetching user session : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), event, promise);
            } else {
                JsonObject userSession = event.right().getValue();
                UserNextcloud.TokenProvider tokenProvider = new UserNextcloud.TokenProvider()
                        .setUserId(userSession.getString(Field.USER_ID, null))
                        .setUserName(userSession.getString(Field.USERNAME, null))
                        .setToken(userSession.getString(Field.PASSWORD, null));
                promise.complete(tokenProvider);
            }
        }));
        return promise.future();
    }

    @Override
    public Future<JsonObject> changeUserPassword(final String host, UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        this.editUserInfo(host, userBody.userId(), EditableDataField.PASSWORD, userBody.password())
                .onSuccess(promise::complete)
                .onFailure(promise::fail);
        return promise.future();
    }

    /**
     * This method allows you to call it by any change of user's info
     *
     * @param host host
     * @param   userId              user nextcloud identifier (not the ENT user identifier)
     * @param   editableDataField   dataField option to wish to edit {@link EditableDataField} enum
     * @param   value               Value to pass to its data field
     * @return  OCS response
     */
    private Future<JsonObject> editUserInfo(String host, String userId, EditableDataField editableDataField, String value) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject payload = new JsonObject()
                .put(Field.KEY, editableDataField.dataField())
                .put(Field.VALUE, value);
        final NextcloudConfig nextcloudConfig = this.nextcloudConfigMapByHost.get(host);
        this.client.putAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint() + USER_ENDPOINT + "/" + userId)
                .basicAuthentication(nextcloudConfig.username(), nextcloudConfig.password())
                .putHeader(Field.OCS_API_REQUEST, String.valueOf(true))
                .as(BodyCodec.jsonObject())
                .addQueryParam(Field.FORMAT, Field.JSON)
                .sendJsonObject(payload, responseAsync -> onEditUserInfoHandler(responseAsync, promise));
        return promise.future();
    }

    /**
     * Proceed async event after HTTP PUT on user info API endpoint has been sent
     *
     * @param   responseAsync   HttpResponse of Jsonobject {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link JsonObject}
     */
    private void onEditUserInfoHandler(AsyncResult<HttpResponse<JsonObject>> responseAsync, Promise<JsonObject> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::onEditUserInfoHandler] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<JsonObject> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String messageToFormat = "[Nextcloud@%s::onEditUserInfoHandler] Response status is not a HTTP 200 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                promise.complete(response.body());
            }
        }
    }

    /**
     * password generator
     *
     * @return generated password
     */
    private String generateUserPassword() {
        return new RandomStringGenerator.Builder()
                .withinRange(33, 45)
                .build()
                .generate(16);
    }
}
