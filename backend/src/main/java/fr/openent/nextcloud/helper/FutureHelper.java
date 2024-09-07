package fr.openent.nextcloud.helper;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.impl.CompositeFutureImpl;

import java.util.List;

public class FutureHelper {
    public static <T> CompositeFuture all(List<Future<T>> futures) {
        return CompositeFutureImpl.all(futures.toArray(new Future[futures.size()]));
    }

    public static <T> CompositeFuture join(List<Future<T>> futures) {
        return CompositeFutureImpl.join(futures.toArray(new Future[futures.size()]));
    }

    public static <T> CompositeFuture any(List<Future<T>> futures) {
        return CompositeFutureImpl.any(futures.toArray(new Future[0]));
    }
}
