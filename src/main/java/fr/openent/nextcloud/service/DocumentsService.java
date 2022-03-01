package fr.openent.nextcloud.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

public interface DocumentsService {

    /**
     * List files/folder
     *
     * @param userId    User identifier
     * @param path      path of nextcloud's user
     * @return  Future List of folder/list {@link JsonArray}
     */
    Future<JsonArray> listFiles(String userId, String path);
}
