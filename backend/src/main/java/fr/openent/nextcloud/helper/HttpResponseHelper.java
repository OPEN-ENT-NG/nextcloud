package fr.openent.nextcloud.helper;

import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.client.HttpResponse;

public class HttpResponseHelper {

    private HttpResponseHelper() { throw new IllegalStateException("Helper HttpResponse class"); }

    public static void reject(Logger log, String messageToFormat, String className, HttpResponse<?> response,
                              Promise<?> promise) {
        String message = String.format(messageToFormat, className, response.statusCode(), response.statusMessage());
        log.error(message);
        promise.fail(response.statusMessage());
    }

}
