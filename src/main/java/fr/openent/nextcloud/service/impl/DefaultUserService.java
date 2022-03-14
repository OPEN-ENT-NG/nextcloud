package fr.openent.nextcloud.service.impl;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.helper.XMLHelper;
import fr.openent.nextcloud.model.OCSResponse;
import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.service.UserService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import java.nio.charset.StandardCharsets;

public class DefaultUserService implements UserService {
    private final Logger log = LoggerFactory.getLogger(DefaultUserService.class);
    private final WebClient client;
    private final NextcloudConfig nextcloudConfig;

    public DefaultUserService(WebClient webclient, NextcloudConfig nextcloudConfig) {
        this.client = webclient;
        this.nextcloudConfig = nextcloudConfig;
    }

    @Override
    public Future<JsonObject> addNewUser(UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        this.client.postAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint())
                .basicAuthentication(this.nextcloudConfig.username(), this.nextcloudConfig.password())
                .putHeader(Field.OCS_API_REQUEST, String.valueOf(true))
                .addQueryParam(Field.FORMAT, Field.JSON)
                .as(BodyCodec.string(StandardCharsets.UTF_8.toString()))
                .sendJsonObject(userBody.toJSON(), responseAsync -> proceedUserCreation(responseAsync, promise));
        return promise.future();
    }

    /**
     * Proceed async event after HTTP adding new user API endpoint has been sent
     *
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link JsonObject}
     */
    private void proceedUserCreation(AsyncResult<HttpResponse<String>> responseAsync, Promise<JsonObject> promise) {
        if (responseAsync.failed()) {
            String message = String.format("[Nextcloud@%s::proceedUserCreation] An error has occurred during fetching endpoint : %s",
                    this.getClass().getSimpleName(), responseAsync.cause().getMessage());
            log.error(message);
            promise.fail(responseAsync.cause());
        } else {
            HttpResponse<String> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String message = String.format("[Nextcloud@%s::proceedUserCreation] Response status is not a HTTP 200 : %s : %s",
                        this.getClass().getSimpleName(), response.statusCode(), response.statusMessage());
                log.error(message);
                promise.fail(response.statusMessage());
            } else {
                JsonObject results = XMLHelper.toJsonObject(response.body());
                OCSResponse ocsResponse = new OCSResponse(results.getJsonObject(Field.OCS, new JsonObject()));
                promise.complete(ocsResponse.toJSON());
            }
        }
    }

    @Override
    public Future<UserNextcloud> getUserInfo(String userId) {
        Promise<UserNextcloud> promise = Promise.promise();
        this.client.getAbs(nextcloudConfig.host() + nextcloudConfig.ocsEndpoint() + "/" + userId)
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
            String message = String.format("[Nextcloud@%s::getUserInfo] An error has occurred during fetching endpoint : %s",
                    this.getClass().getSimpleName(), responseAsync.cause().getMessage());
            log.error(message);
            promise.fail(responseAsync.cause());
        } else {
            HttpResponse<JsonObject> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String message = String.format("[Nextcloud@%s::getUserInfo] Response status is not a HTTP 200 : %s : %s",
                        this.getClass().getSimpleName(), response.statusCode(), response.statusMessage());
                log.error(message);
                promise.fail(response.statusMessage());
            } else {
                OCSResponse ocsResponse = new OCSResponse(response.body().getJsonObject(Field.OCS, new JsonObject()));
                promise.complete(new UserNextcloud(ocsResponse.data()));
            }
        }
    }
}
