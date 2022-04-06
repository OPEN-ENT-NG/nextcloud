package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.security.OwnerFilter;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;

import fr.openent.nextcloud.service.UserService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserUtils;

import java.util.List;

public class DocumentsController extends ControllerHelper {

    private final DocumentsService documentsService;
    private final UserService userService;

    public DocumentsController(ServiceFactory serviceFactory) {
        this.documentsService = serviceFactory.documentsService();
        this.userService = serviceFactory.userService();
    }

    @Get("/files/user/:userid")
    @ApiDoc("API to list file/folder")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void listFiles(HttpServerRequest request) {
        String path = request.getParam(Field.PATH);
        UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(user.getUserId())
                        .compose(userSession -> documentsService.listFiles(userSession, path))
                        .onSuccess(files -> renderJson(request, new JsonObject().put(Field.DATA, files)))
                        .onFailure(err -> renderError(request)));
    }

    @Get("/files/user/:userid/file/:fileName/download")
    @ApiDoc("API to get or download file")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void getFile(HttpServerRequest request) {
        String fileName = request.getParam(Field.FILENAME);
        String path = request.getParam(Field.PATH);
        String contentType = request.getParam(Field.CONTENTTYPE);
        UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(user.getUserId())
                        .compose(userSession -> documentsService.getFile(userSession, path.replace(" ", "%20")))
                        .onSuccess(file -> request.response()
                                .putHeader("Content-type", contentType + "; charset=utf-8")
                                .putHeader("Content-Disposition", "attachment; filename=" + fileName)
                                .end(file))
                        .onFailure(err -> renderError(request)));
    }

    @Get("/files/user/:userid/multiple/download")
    @ApiDoc("API to download multiple files")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void downloadMultipleFile(HttpServerRequest request) {
        String path = request.getParam(Field.PATH);
        List<String> files = request.params().getAll(Field.FILE);
        if ((path != null && !path.isEmpty()) && (files != null && !files.isEmpty())) {
            UserUtils.getUserInfos(eb, request, user ->
                    userService.getUserSession(user.getUserId())
                            .compose(userSession -> documentsService.getFiles(userSession, path, files))
                            .onSuccess(file -> {
                                String pathName = path.equals("/") ? user.getLogin() : path;
                                HttpServerResponse resp = request.response();
                                resp.putHeader("Content-Disposition", "attachment; filename=\"" + pathName + ".zip\"");
                                resp.putHeader("Content-Type", "application/octet-stream");
                                resp.putHeader("Content-Description", "File Transfer");
                                resp.putHeader("Content-Transfer-Encoding", "binary");
                                resp.end(file);
                            })
                            .onFailure(err -> renderError(request)));
        } else {
            badRequest(request);
        }
    }

    @Put("/files/user/:userid/move")
    @ApiDoc("Move documents file")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void moveDocuments(HttpServerRequest request) {
        String path = request.getParam(Field.PATH);
        String destPath = request.getParam(Field.DESTPATH);
        if ((path != null && !path.isEmpty()) && (destPath != null && !destPath.isEmpty())) {
            UserUtils.getUserInfos(eb, request, user ->
                    userService.getUserSession(user.getUserId())
                            .compose(userSession -> {
                                String formattedPath = path.replace(" ", "%20");
                                String formattedDestPath = destPath.replace(" ", "%20");
                                return documentsService.moveDocument(userSession, formattedPath, formattedDestPath);
                            })
                            .onSuccess(res -> renderJson(request, res))
                            .onFailure(err -> renderError(request, new JsonObject().put(Field.MESSAGE, err.getMessage()))));
        } else {
            badRequest(request);
        }
    }


    @Delete("/files/user/:userid/delete")
    @ApiDoc("delete documents API")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void deleteDocuments(HttpServerRequest request) {
        List<String> paths = request.params().getAll(Field.PATH);
        if ((paths != null && !paths.isEmpty())) {
            UserUtils.getUserInfos(eb, request, user ->
                    userService.getUserSession(user.getUserId())
                            .compose(userSession -> documentsService.deleteDocuments(userSession, paths))
                            .onSuccess(res -> renderJson(request, res))
                            .onFailure(err -> renderError(request, new JsonObject().put(Field.MESSAGE, err.getMessage()))));
        } else {
            badRequest(request);
        }
    }




}
