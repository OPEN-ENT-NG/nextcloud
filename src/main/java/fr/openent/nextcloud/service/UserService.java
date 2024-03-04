package fr.openent.nextcloud.service;

import fr.openent.nextcloud.model.UserNextcloud;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface UserService {

    /**
     * add and/or provide new user nextcloud and its session
     *
     * @param host host
     * @param   userBody        User Body request {@link UserNextcloud.RequestBody}
     * @return  Future Instance of User creation response {@link JsonObject}
     */
    Future<JsonObject> provideUserSession(final String host, UserNextcloud.RequestBody userBody);

    /**
     * change user info password
     *
     * @param host host
     * @param   userBody  User Body request {@link UserNextcloud.RequestBody}
     * @return  Future Instance of User edit info response {@link JsonObject}
     */
    Future<JsonObject> changeUserPassword(final String host, UserNextcloud.RequestBody userBody);

    /**
     * Get User Nextcloud info
     *
     * @param host host
     * @param   userId  User identifier (login ENT)
     * @return  Future Instance of User from Nextcloud {@link UserNextcloud}
     */
    Future<UserNextcloud> getUserInfo(String host, String userId);

    /**
     * Get User Session Token Provider
     *
     * @param   userId  User identifier (ENT part)
     * @return  Future Instance of User Session Token Provider from Nextcloud {@link UserNextcloud.TokenProvider}
     */
    Future<UserNextcloud.TokenProvider> getUserSession(String userId);
}
