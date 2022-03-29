package fr.openent.nextcloud.service;

import fr.openent.nextcloud.model.UserNextcloud;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface TokenProviderService {

    /**
     * add new user session to nextcloud
     *
     * @param   userId      User identifier (ENT user identifier)
     * @param   userBody    User Body request {@link UserNextcloud.RequestBody}
     * @return  Future Instance of User creation response {@link JsonObject}
     */
    Future<JsonObject> provideNextcloudSession(String userId, UserNextcloud.RequestBody userBody);
}
