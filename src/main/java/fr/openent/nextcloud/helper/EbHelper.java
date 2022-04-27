package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.service.impl.DefaultDocumentsService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class EbHelper {
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
                .put(Field.ACTION, "addFolder")
                .put(Field.NAME, folder.getString("name"))
                .put(Field.OWNER, userId)
                .put(Field.OWNERNAME, userName)
                .put(Field.PARENTFOLDERID, folder.getString("parent_id"));
        return ebHandling(eb, action);
    }

    /**
     * Call event bus with action
     * @param eb        EventBus
     * @param action    The action to perform
     * @return          Future with the body of the response from the eb
     */
    private static Future<JsonObject> ebHandling(EventBus eb, JsonObject action) {
        Promise<JsonObject> promise = Promise.promise();
        eb.request(WORKSPACE_BUS_ADDRESS, action, message -> {
            JsonObject body = new JsonObject(message.result().body().toString());
            if (!message.succeeded() || !"ok".equals(body.getString("status"))) {
                String messageToFormat = "[Nextcloud@%s::ebHandling] An error occurred when calling document by event bus : %s";
                PromiseHelper.reject(log, messageToFormat, EbHelper.class.getName(), message, promise);
            } else {
                promise.complete(body);
            }
        });
        return promise.future();
    }
}
