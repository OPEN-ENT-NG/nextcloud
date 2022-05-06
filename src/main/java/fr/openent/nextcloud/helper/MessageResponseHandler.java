package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.core.constants.Field;
import fr.wseduc.webutils.Either;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class MessageResponseHandler {
    public static Handler<AsyncResult<Message<JsonObject>>> messageJsonObjectHandler(Handler<Either<String, JsonObject>> handler) {
        return event -> {
            if (event.succeeded() && Field.OK.equals(event.result().body().getString(Field.STATUS))) {
                handler.handle(new Either.Right<>(event.result().body().getJsonObject(Field.RESULT)));
            } else {
                if (event.failed()) {
                    handler.handle(new Either.Left<>(event.cause().getMessage()));
                    return;
                }
                handler.handle(new Either.Left<>(event.result().body().getString(Field.MESSAGE)));
            }
        };
    }
}
