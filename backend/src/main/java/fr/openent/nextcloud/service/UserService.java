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
     * @param host      host
     * @param   userId  User identifier (ENT part)
     * @return  Future Instance of User Session Token Provider from Nextcloud {@link UserNextcloud.TokenProvider}
     */
    Future<UserNextcloud.TokenProvider> getUserSession(String host, String userId);

    /**
     * Exchange an OAuth2 authorization code for an access/refresh token pair.
     *
     * @param host host
     * @param code  authorization code returned by Nextcloud's OAuth2 consent page
     * @return  Future Instance of the exchanged OAuth2 token {@link UserNextcloud.OAuthToken}
     */
    Future<UserNextcloud.OAuthToken> exchangeAuthorizationCode(String host, String code);

    /**
     * Persist an OAuth2 token pair for the given ENT user.
     *
     * @param userId    User identifier (ENT part)
     * @param token     OAuth2 token to persist {@link UserNextcloud.OAuthToken}
     * @return  Future completed once persisted
     */
    Future<Void> persistOauthTokens(String userId, UserNextcloud.OAuthToken token);

    /**
     * Check whether the given ENT user already has a NextCloud OAuth2 token stored.
     *
     * @param userId    User identifier (ENT part)
     * @return  Future of true if a token is stored, false otherwise
     */
    Future<Boolean> getOauthStatus(String userId);
}
