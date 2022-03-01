package fr.openent.nextcloud.security;

import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class OwnerFilter implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest httpServerRequest, Binding binding, UserInfos userInfos, Handler<Boolean> handler) {
        String userId = httpServerRequest.getParam("userid");
        handler.handle(userInfos != null && userInfos.getLogin().equals(userId));
    }
}
