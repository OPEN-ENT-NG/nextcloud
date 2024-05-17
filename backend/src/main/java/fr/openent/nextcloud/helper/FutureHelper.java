package fr.openent.nextcloud.helper;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.List;

public class FutureHelper {
    public static <T> CompositeFuture all(List<Future<T>> futures) {

        return Future.all(futures);
    }

    public static <T> CompositeFuture join(List<Future<T>> futures) {
        return Future.join(futures);
    }

    public static <T> CompositeFuture any(List<Future<T>> futures) {
        return Future.any(futures);
    }
}
