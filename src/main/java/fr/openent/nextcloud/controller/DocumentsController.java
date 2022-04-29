package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.helper.FileHelper;
import fr.openent.nextcloud.security.OwnerFilter;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.openent.nextcloud.service.UserService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserUtils;

import java.util.List;


public class DocumentsController extends ControllerHelper {

    private final DocumentsService documentsService;
    private final UserService userService;
    private final Storage storage;

    public DocumentsController(ServiceFactory serviceFactory) {
        this.documentsService = serviceFactory.documentsService();
        this.userService = serviceFactory.userService();
        this.storage = serviceFactory.storage();
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
                        .onSuccess(fileResponse -> request.response()
                                .putHeader("Content-type", contentType + "; charset=utf-8")
                                .putHeader("Content-Disposition", "attachment; filename=" + fileName)
                                .end(fileResponse.body()))
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
                            .onSuccess(fileResponse -> {
                                String pathName = path.equals("/") ? user.getLogin() : path;
                                HttpServerResponse resp = request.response();
                                resp.putHeader("Content-Disposition", "attachment; filename=\"" + pathName + ".zip\"");
                                resp.putHeader("Content-Type", "application/octet-stream");
                                resp.putHeader("Content-Description", "File Transfer");
                                resp.putHeader("Content-Transfer-Encoding", "binary");
                                resp.end(fileResponse.body());
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

    @Put("/files/user/:userid/upload")
    @ApiDoc("Upload file")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void uploadDocuments(HttpServerRequest request) {
        request.pause();
        String path = request.getParam(Field.PATH);
        UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(user.getUserId())
                        .compose(userSession -> {
                            request.resume();
                            return FileHelper.uploadMultipleFiles(Field.FILECOUNT, request, storage, vertx)
                                    .compose(files -> documentsService.uploadFiles(userSession, files, path));
                        })
                        .onSuccess(res -> renderJson(request, res))
                        .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()))));

    }

    @Put("/files/user/:userid/move/local")
    @ApiDoc("Copy a file from Nextcloud to ENT storage")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void moveToLocal(HttpServerRequest request) {
        List<String> listFiles = request.params().getAll(Field.PATH);
        String parentId = request.params().get("parentId");
        if (!listFiles.isEmpty())
            UserUtils.getUserInfos(eb, request, user -> {
                userService.getUserSession(user.getUserId())
                        .compose(userSession -> {
                            return documentsService.moveDocumentENT(userSession, user, listFiles, parentId);
                        })
                        .onSuccess(res -> renderJson(request, res))
                        .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
            });
        else
            badRequest(request);
    }

    @Put("/files/user/:userid/copy/local")
    @ApiDoc("Copy a file from Nextcloud to ENT storage")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void copyToLocal(HttpServerRequest request) {
        List<String> listFiles = request.params().getAll(Field.PATH);
        String parentId = request.params().get("parentId");
        if (!listFiles.isEmpty())
            UserUtils.getUserInfos(eb, request, user -> {
                userService.getUserSession(user.getUserId())
                        .compose(userSession -> {
                            return documentsService.copyDocumentENT(userSession, user, listFiles, parentId);
                        })
                        .onSuccess(res -> renderJson(request, res))
                        .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
            });
        else
            badRequest(request);
    }

}
