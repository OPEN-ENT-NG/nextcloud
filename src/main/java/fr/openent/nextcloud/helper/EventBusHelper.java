package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.service.impl.DefaultDocumentsService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class EventBusHelper {
    private static final String WORKSPACE_BUS_ADDRESS = "org.entcore.workspace";
    private static final Logger log = LoggerFactory.getLogger(DefaultDocumentsService.class);

    /**
     * Create a folder ine the workspace
     * @param folder    JsonObject with folder's infos
     * @param userId    User's identifier
     * @param userName  User's username
     * @return          Future with the status of the folder creation
     */
    public static Future<JsonObject> createFolder(EventBus eb, JsonObject folder, String userId, String userName) {
        JsonObject action = new JsonObject()
                .put(Field.ACTION, Field.ADDFOLDER)
                .put(Field.NAME, folder.getString(Field.NAME))
                .put(Field.OWNER, userId)
                .put(Field.OWNERNAME, userName)
                .put(Field.PARENTFOLDERID, folder.getString(Field.PARENT_ID));
        return requestJsonObject(eb, action);
    }

    public static Future<JsonArray> deleteDocument(EventBus eb, String id, String userId) {
        JsonObject action =  new JsonObject()
                .put(Field.ACTION, Field.DELETE)
                .put(Field.ID, id)
                .put(Field.USERID_CAPS, userId);
        return requestJsonArray(eb, action);
    }

    /**
     * Call event bus with action
     * @param eb        EventBus
     * @param action    The action to perform
     * @return          Future with the body of the response from the eb
     */
    public static Future<JsonObject> requestJsonObject(EventBus eb, JsonObject action) {
        Promise<JsonObject> promise = Promise.promise();
        eb.request(WORKSPACE_BUS_ADDRESS, action, MessageResponseHandler.messageJsonObjectHandler(PromiseHelper.handlerJsonObject(promise)));
        return promise.future();
    }

    public static Future<JsonArray> requestJsonArray(EventBus eb, JsonObject action) {
        Promise<JsonArray> promise = Promise.promise();
        eb.request(WORKSPACE_BUS_ADDRESS, action, MessageResponseHandler.messageJsonArrayHandler(PromiseHelper.handlerJsonArray(promise)));
        return promise.future();
    }


}
