package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.security.OwnerFilter;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.openent.nextcloud.service.UserService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserUtils;


public class UserController extends ControllerHelper {

    private final UserService userService;

    public UserController(ServiceFactory serviceFactory) {
        this.userService = serviceFactory.userService();
    }

    @Get("/user/:userid/provide/token")
    @ApiDoc("Provide nextcloud token")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void provideUserSession(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            UserNextcloud.RequestBody userCreationBody = new UserNextcloud.RequestBody()
                    .setUserId(user.getUserId())
                    .setDisplayName(user.getUsername());
            userService.provideUserSession(userCreationBody)
                    .onSuccess(userNextcloud -> renderJson(request, new JsonObject().put(Field.STATUS, Field.OK)))
                    .onFailure(err -> renderError(request));
        });
    }

    @Get("/user/:userid")
    @ApiDoc("get user info view")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void getUserInfo(HttpServerRequest request) {
        String userId = request.getParam(Field.USERID);
        userService.getUserInfo(userId)
                .onSuccess(userNextcloud -> renderJson(request, userNextcloud.toJSON()))
                .onFailure(err -> renderError(request));
    }

}
