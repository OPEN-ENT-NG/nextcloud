package fr.openent.nextcloud.helper;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class EventBusHelper {
    private EventBusHelper() {
        throw new UnsupportedOperationException("Class instantiation not allowed");
    }

    private static final String WORKSPACE_BUS_ADDRESS = "org.entcore.workspace";

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
