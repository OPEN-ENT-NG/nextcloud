package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.security.OwnerFilter;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserUtils;

public class DocumentsController extends ControllerHelper {

    private final DocumentsService documentsService;

    public DocumentsController(ServiceFactory serviceFactory) {
        this.documentsService = serviceFactory.documentsService();
    }

    @Get("/files/user/:userid")
    @ApiDoc("Render view")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void listFiles(HttpServerRequest request) {
        String userId = request.getParam(Field.USERID);
        String path = request.getParam(Field.PATH);
        UserUtils.getUserInfos(eb, request, user ->
                documentsService.listFiles(user.getUserId(), userId, path)
                        .onSuccess(files -> renderJson(request, new JsonObject().put(Field.DATA, files)))
                        .onFailure(err -> renderError(request)));
    }

    @Get("/files/user/:userid/download")
    @ApiDoc("Render view")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void getFile(HttpServerRequest request) {
        String userId = request.getParam(Field.USERID);
        String path = request.getParam(Field.PATH);
        UserUtils.getUserInfos(eb, request, user ->
                documentsService.getFile(user.getUserId(), userId, path.replace(" ", "%20"))
                        .onSuccess(file -> request.response().end(file))
                        .onFailure(err -> renderError(request)));
    }

    @Put("/files/user/:userid")
    @ApiDoc("Render view")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void uploadFile(HttpServerRequest request) {
        String userId = request.getParam(Field.USERID);
        String path = request.getParam(Field.PATH);
        UserUtils.getUserInfos(eb, request, user ->
                documentsService.getFile(user.getUserId(), userId, path.replace(" ", "%20"))
                        .onSuccess(file -> request.response().end(file))
                        .onFailure(err -> renderError(request)));
    }




}
