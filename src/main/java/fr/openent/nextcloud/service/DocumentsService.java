package fr.openent.nextcloud.service;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface DocumentsService {

    /**
     * List files/folder
     *
     * @param userId            User identifier (ENT part)
     * @param userNextcloudId   User nextcloud identifier (login name)
     * @param path              path of nextcloud's user
     * @return  Future List of folder/list {@link JsonArray}
     */
    Future<JsonArray> listFiles(String userId, String userNextcloudId, String path);

    /**
     * get/download file
     *
     * @param userId            User identifier (ENT part)
     * @param userNextcloudId   User nextcloud identifier (login name)
     * @param path              path of nextcloud's user
     */
    Future<Buffer> getFile(String userId, String userNextcloudId, String path);

    /**
     * upload file
     *
     * @param userId    User identifier
     * @param path      path of nextcloud's user
     */
    Future<JsonObject> uploadFile(String userId, String path);
}
