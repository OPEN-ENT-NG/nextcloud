package fr.openent.nextcloud.security;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.core.enums.WorkflowActions;
import fr.openent.nextcloud.helper.WorkflowHelper;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class CanAccessNextcloud implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest httpServerRequest, Binding binding, UserInfos userInfos, Handler<Boolean> handler) {
        String userId = httpServerRequest.getParam(Field.USERID);
        handler.handle(userInfos != null && userInfos.getUserId().equals(userId) &&
                WorkflowHelper.hasRight(userInfos, WorkflowActions.ACCESS.getAction()));
    }
}
