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

public class DefaultUserService implements UserService {
    private final Logger log = LoggerFactory.getLogger(DefaultUserService.class);
    private final WebClient client;
    private final NextcloudConfig nextcloudConfig;
    private final TokenProviderService tokenProviderService;
    private final DocumentsService documentsService;

    private static final String USER_ENDPOINT = "/cloud/users";

    public DefaultUserService(ServiceFactory serviceFactory) {
        this.client = serviceFactory.webClient();
        this.nextcloudConfig = serviceFactory.nextcloudConfig();
        this.tokenProviderService = serviceFactory.tokenProviderService();
        this.documentsService = serviceFactory.documentsService();
    }

    @Override
    public Future<JsonObject> provideUserSession(UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        this.getUserInfo(userBody.userId())
                .compose(userNextcloud -> resolveUserSession(userBody, userNextcloud))
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
     * @param   userBody        User Body request {@link UserNextcloud.RequestBody}
     * @param   userNextcloud   user nextcloud info {@link UserNextcloud}
     * @return  Empty response (succeed will resolve user's session by persisting it via database)
     */
    private Future<Void> resolveUserSession(UserNextcloud.RequestBody userBody, UserNextcloud userNextcloud) {
        Promise<Void> promise = Promise.promise();
        if (userNextcloud.id() != null) {
            this.getUserSession(userBody.userId())
                    .compose(this::checkSessionValidity)
                    .onSuccess(userSession -> {
                        if (userSession.isEmpty()) {
                            userBody.setPassword(generateUserPassword());
                            this.changeUserPassword(userBody)
                                    .compose(res -> this.tokenProviderService.provideNextcloudSession(userBody))
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
            this.addNewUser(userBody)
                    .compose(res -> this.tokenProviderService.provideNextcloudSession(userBody))
                    .onSuccess(res -> promise.complete())
                    .onFailure(promise::fail);
        }
        return promise.future();
    }

    /**
     * Check if the user's token is still up-to-date in the database
     * @param userSession   User session
     * @return              Future with the updated session if update needed, else old session.
     */
    private Future<UserNextcloud.TokenProvider> checkSessionValidity(UserNextcloud.TokenProvider userSession) {
        Promise<UserNextcloud.TokenProvider> promise = Promise.promise();
        if (userSession.isEmpty()) {
            promise.complete(new UserNextcloud.TokenProvider());
        } else {
            documentsService.parametrizedListFiles(userSession, null, response -> {
                if (response.failed()) {
                    String messageToFormat = "[Nextcloud@%s::checkSessionValidity] Error during token validity check : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response.cause(), promise);
                } else {
                    if (response.result().statusCode() != 200 && response.result().statusCode() != 207) {
                        promise.complete(new UserNextcloud.TokenProvider());
                    } else {
                        promise.complete(userSession);
                    }
                }
            });
        }
        return promise.future();
    }

    @Override
    public Future<JsonObject> addNewUser(UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        this.client.postAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint() + USER_ENDPOINT)
                .basicAuthentication(this.nextcloudConfig.username(), this.nextcloudConfig.password())
                .putHeader(Field.OCS_API_REQUEST, String.valueOf(true))
                .addQueryParam(Field.FORMAT, Field.JSON)
                .as(BodyCodec.string(StandardCharsets.UTF_8.toString()))
                .sendJsonObject(userBody.toJSON(), responseAsync -> proceedUserCreation(userBody, responseAsync, promise));
        return promise.future();
    }

    /**
     * Proceed async event after HTTP adding new user API endpoint has been sent
     *
     * @param   userBody        request user body sent {@link UserNextcloud.RequestBody}
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link JsonObject}
     */
    private void proceedUserCreation(UserNextcloud.RequestBody userBody, AsyncResult<HttpResponse<String>> responseAsync,
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
                        .put(Field.DATA, userBody.toJSON());
                promise.complete(result);
            }
        }
    }

    @Override
    public Future<UserNextcloud> getUserInfo(String userId) {
        Promise<UserNextcloud> promise = Promise.promise();
        this.client.getAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint() + USER_ENDPOINT + "/" + userId)
                .basicAuthentication(this.nextcloudConfig.username(), this.nextcloudConfig.password())
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
    public Future<JsonObject> changeUserPassword(UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        this.editUserInfo(userBody.userId(), EditableDataField.PASSWORD, userBody.password())
                .onSuccess(promise::complete)
                .onFailure(promise::fail);
        return promise.future();
    }

    /**
     * This method allows you to call it by any change of user's info
     *
     * @param   userId              user nextcloud identifier (not the ENT user identifier)
     * @param   editableDataField   dataField option to wish to edit {@link EditableDataField} enum
     * @param   value               Value to pass to its data field
     * @return  OCS response
     */
    private Future<JsonObject> editUserInfo(String userId, EditableDataField editableDataField, String value) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject payload = new JsonObject()
                .put(Field.KEY, editableDataField.dataField())
                .put(Field.VALUE, value);
        this.client.putAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint() + USER_ENDPOINT + "/" + userId)
                .basicAuthentication(this.nextcloudConfig.username(), this.nextcloudConfig.password())
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
