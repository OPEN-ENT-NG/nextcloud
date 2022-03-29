package fr.openent.nextcloud.helper;

import fr.wseduc.webutils.Either;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class PromiseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromiseHelper.class);

    private PromiseHelper() {
        throw new IllegalStateException("Utility PromiseHelper class");
    }

    public static Handler<Either<String, JsonObject>> handlerJsonObject(Promise<JsonObject> promise) {
        return event -> {
            if (event.isRight()) {
                promise.complete(event.right().getValue());
            } else {
                String message = "[Nextcloud - handlerJsonObject] Failure : " + event.left().getValue();
                LOGGER.error(message, event.left().getValue());
                promise.fail(event.left().getValue());
            }
        };
    }

    public static Handler<Either<String, JsonArray>> handlerJsonArray(Promise<JsonArray> promise) {
        return event -> {
            if (event.isRight()) {
                promise.complete(event.right().getValue());
            } else {
                String message = "[Nextcloud - handlerJsonArray] Failure : " + event.left().getValue();
                LOGGER.error(message, event.left().getValue());
                promise.fail(event.left().getValue());
            }
        };
    }

    public static void reject(Logger log, String messageToFormat, String className, AsyncResult<?> responseAsync, Promise<?> promise) {
        String message = String.format(messageToFormat, className, responseAsync.cause().getMessage());
        log.error(message);
        promise.fail(responseAsync.cause());
    }

    public static void reject(Logger log, String messageToFormat, String className, Either<String, ?> either, Promise<?> promise) {
        String message = String.format(messageToFormat, className, either.left().getValue());
        log.error(message);
        promise.fail(either.left().getValue());
    }
}