package fr.openent.nextcloud.service.impl;

import fr.openent.nextcloud.Nextcloud;
import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.helper.HttpResponseHelper;
import fr.openent.nextcloud.helper.PromiseHelper;
import fr.openent.nextcloud.model.OCSResponse;
import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.service.TokenProviderService;
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
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

public class DefaultTokenProviderService implements TokenProviderService {
    private final Logger log = LoggerFactory.getLogger(DefaultTokenProviderService.class);
    private final WebClient client;
    private final NextcloudConfig nextcloudConfig;

    private static final String TOKEN_ENDPOINT = "/core";

    public DefaultTokenProviderService(WebClient webclient, NextcloudConfig nextcloudConfig) {
        this.client = webclient;
        this.nextcloudConfig = nextcloudConfig;
    }

    @Override
    public Future<JsonObject> provideNextcloudSession(String userId, UserNextcloud.RequestBody userBody) {
        Promise<JsonObject> promise = Promise.promise();
        this.client.getAbs(nextcloudConfig.host() + this.nextcloudConfig.ocsEndpoint() + TOKEN_ENDPOINT + "/getapppassword")
                .basicAuthentication(userBody.userId(), userBody.password())
                .putHeader(Field.OCS_API_REQUEST, String.valueOf(true))
                .as(BodyCodec.jsonObject())
                .addQueryParam(Field.FORMAT, Field.JSON)
                .send(responseAsync -> onProvideNextcloudSessionHandler(userId, userBody, responseAsync, promise));
        return promise.future();
    }

    /**
     * persist its user session plus It's token to database
     *
     * @param   userId          User identifier (ENT user identifier)
     * @param   userBody        User Body request {@link UserNextcloud.RequestBody}
     * @param   responseAsync   Response OCS given after creating nextcloud user session
     * @param   promise         Promise to complete
     */
    private void onProvideNextcloudSessionHandler(String userId, UserNextcloud.RequestBody userBody, AsyncResult<HttpResponse<JsonObject>> responseAsync,
                                                  Promise<JsonObject> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::provideNextcloudSession] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<JsonObject> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String messageToFormat = "[Nextcloud@%s::provideNextcloudSession] Response status is not a HTTP 200 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                OCSResponse ocsResponse = new OCSResponse(response.body().getJsonObject(Field.OCS, new JsonObject()));
                UserNextcloud.TokenProvider tokenProvider = new UserNextcloud.TokenProvider()
                        .setLoginName(userBody.userId())
                        .setToken(ocsResponse.data().getString(Field.APPPASSWORD, ""));
                this.persistToken(userId, tokenProvider)
                        .onSuccess(promise::complete)
                        .onFailure(promise::fail);
            }
        }
    }

    /**
     * persist its user session plus It's token to database
     *
     * @param   userId          User identifier
     * @param   tokenProvider   Token provider with OCS response received {@link UserNextcloud.TokenProvider}
     * @return  Future JsonObject SQL reply
     */
    private Future<JsonObject> persistToken(String userId, UserNextcloud.TokenProvider tokenProvider) {
        Promise<JsonObject> promise = Promise.promise();
        String query = "INSERT INTO " + Nextcloud.DB_SCHEMA + ".user (user_id, login, password, last_modified) " +
                " VALUES (?, ?, ?, now()) " +
                " ON CONFLICT (user_id) " +
                " DO UPDATE SET login = ?, password = ?, last_modified = now()";

        JsonArray param = new JsonArray()
                .add(userId)
                .add(tokenProvider.loginName())
                .add(tokenProvider.token())
                .add(tokenProvider.loginName())
                .add(tokenProvider.token());

        Sql.getInstance().prepared(query, param, SqlResult.validUniqueResultHandler(PromiseHelper.handlerJsonObject(promise)));
        return promise.future();
    }
}
