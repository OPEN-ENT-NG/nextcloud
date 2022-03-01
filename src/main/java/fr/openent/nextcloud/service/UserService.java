package fr.openent.nextcloud.service;

import fr.openent.nextcloud.model.UserNextcloud;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface UserService {

    /**
     * add new user nextcloud
     *
     * @param   userBody  User Body request {@link UserNextcloud.RequestBody}
     * @return  Future Instance of User creation response {@link JsonObject}
     */
    Future<JsonObject> addNewUser(UserNextcloud.RequestBody userBody);

    /**
     * Get User Nextcloud info
     *
     * @param   userId  User identifier
     * @return  Future Instance of User from Nextcloud {@link UserNextcloud}
     */
    Future<UserNextcloud> getUserInfo(String userId);
}
