package fr.openent.nextcloud.controller;

import fr.openent.nextcloud.Nextcloud;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.helper.Attachment;
import fr.openent.nextcloud.helper.Metadata;
import fr.openent.nextcloud.helper.StringHelper;
import fr.openent.nextcloud.security.OwnerFilter;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.openent.nextcloud.service.UserService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.events.EventHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserUtils;
import org.entcore.common.utils.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentsController extends ControllerHelper {

    private final DocumentsService documentsService;
    private final UserService userService;
    private final Storage storage;
    private final EventHelper eventHelper;
    public static final String RESOURCE_DOC = "document";
    public static final String RESOURCE_FOLDER = "folder";
    public static final String BUS_ADDRESS = "fr.openent.nextcloud";
    private static final String COPY_FROM_STORAGE_ACTION = "copyFromStorage";
    private static final String SESSION_MISSING = "nextcloud.session.missing";
    private static final String PATH_INVALID = "nextcloud.path.invalid";
    private final EventBus eventBus;

    public DocumentsController(ServiceFactory serviceFactory) {
        this.documentsService = serviceFactory.documentsService();
        this.userService = serviceFactory.userService();
        this.storage = serviceFactory.storage();
        final EventStore eventStore = EventStoreFactory.getFactory().getEventStore(Nextcloud.class.getSimpleName());
        this.eventHelper = new EventHelper(eventStore);
        this.eventBus = serviceFactory.eventBus();

        initializeEventBusConsumers();
    }

    @Get("/files/user/:userid")
    @ApiDoc("API to list file/folder")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void listFiles(HttpServerRequest request) {
        final String path = request.getParam(Field.PATH);
        UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(Renders.getHost(request), user.getUserId())
                        .compose(userSession -> documentsService.listFiles(Renders.getHost(request), userSession, path))
                        .onSuccess(files -> {
                            renderJson(request, new JsonObject().put(Field.DATA, files));
                            if (StringUtils.isEmpty(path)) eventHelper.onAccess(request);
                        })
                        .onFailure(err -> renderError(request)));
    }

    @Get("/files/user/:userid/file/:fileName/download")
    @ApiDoc("API to get or download file")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void getFile(HttpServerRequest request) {
        String fileName = StringHelper.decodeUrlForNc(request.getParam(Field.FILENAME));
        String path = request.getParam(Field.PATH);
        String contentType = request.getParam(Field.CONTENTTYPE);
        boolean isFolder = Boolean.parseBoolean(request.getParam(Field.ISFOLDER));
        UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(Renders.getHost(request), user.getUserId())
                        .compose(userSession -> {
                            if (isFolder) {
                                return documentsService.getFolder(Renders.getHost(request), userSession, path);
                            } else {
                                return documentsService.getFile(Renders.getHost(request), userSession, StringHelper.encodeUrlForNc(path));
                            }
                        })
                        .onSuccess(fileResponse -> {
                            HttpServerResponse resp = request.response();
                            if (isFolder) {
                                resp.putHeader("Content-Type", "application/octet-stream")
                                        .putHeader("Content-Disposition", "attachment; filename=\" "+ fileName +" .zip\"")
                                        .putHeader("Content-Description", "File Transfer")
                                        .putHeader("Content-Transfer-Encoding", "binary");
                            } else {
                                resp.putHeader("Content-type", contentType + "; charset=utf-8")
                                        .putHeader("Content-Disposition", "attachment; filename=" + fileName);
                            }
                            resp.end(fileResponse.body());
                        })
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
                    userService.getUserSession(Renders.getHost(request), user.getUserId())
                            .compose(userSession -> documentsService.getFiles(Renders.getHost(request), userSession, path, files))
                            .onSuccess(fileResponse -> {
                                HttpServerResponse resp = request.response();
                                resp.putHeader("Content-Disposition", "attachment; filename=\"" + Field.ARCHIVE + ".zip\"");
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
                    userService.getUserSession(Renders.getHost(request), user.getUserId())
                            .compose(userSession -> {
                                        return documentsService.moveDocument(Renders.getHost(request), userSession, path, destPath);
                                    }
                            )
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
                    userService.getUserSession(Renders.getHost(request), user.getUserId())
                            .compose(userSession -> documentsService.deleteDocuments(Renders.getHost(request), userSession, paths))
                            .onSuccess(res -> renderJson(request, res))
                            .onFailure(err -> renderError(request, new JsonObject().put(Field.MESSAGE, err.getMessage()))));
        } else {
            badRequest(request);
        }
    }

    @Delete("/files/user/:userid/trash/delete-documents")
    @ApiDoc("Delete documents from trash")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void deleteDocumentsFromTrashbin(HttpServerRequest request) {
        List<String> paths = request.params().getAll(Field.PATH);
        if ((paths != null && !paths.isEmpty())) {
            UserUtils.getUserInfos(eb, request, user -> userService.getUserSession(Renders.getHost(request), user.getUserId())
                    .compose(userSession -> documentsService.deleteDocumentsFromTrashbin(Renders.getHost(request),
                            userSession, paths))
                    .onSuccess(res -> renderJson(request, res))
                    .onFailure(err -> renderError(request, new JsonObject().put(Field.MESSAGE, err.getMessage()))));
        } else {
            badRequest(request);
        }
    }

    @Put("/files/user/:userid/restore")
    @ApiDoc("Restore documents from trash")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void restoreDocuments(HttpServerRequest request) {
        List<String> paths = request.params().getAll(Field.PATH);
        if ((paths != null && !paths.isEmpty())) {
            UserUtils.getUserInfos(
                    eb,
                    request,
                    user -> userService
                            .getUserSession(Renders.getHost(request), user.getUserId())
                            .compose(userSession -> documentsService.restoreDocuments(Renders.getHost(request),
                                    userSession,
                                    paths))
                            .onSuccess(res -> renderJson(request, new JsonObject().put(Field.STATUS, Field.OK)))
                            .onFailure(err -> renderError(request,
                                    new JsonObject().put(Field.MESSAGE, err.getMessage()))));
        } else {
            badRequest(request);
        }
    }

    @Get("/files/user/:userid/trash")
    @ApiDoc("API to list trash files")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void listTrash(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> userService.getUserSession(Renders.getHost(request), user.getUserId())
                .compose(userSession -> documentsService.listTrash(Renders.getHost(request), userSession))
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.MESSAGE, err.getMessage()))));
    }

    @Delete("/files/user/:userid/trash/delete")
    @ApiDoc("delete trash API")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void deleteTrash(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(Renders.getHost(request), user.getUserId())
                        .compose(userSession -> documentsService.deleteTrash(Renders.getHost(request), userSession))
                        .onSuccess(res -> renderJson(request, new JsonObject().put(Field.STATUS, Field.OK)))
                        .onFailure(err -> renderError(request, new JsonObject().put(Field.MESSAGE, err.getMessage()))));
    }

    @Put("/files/user/:userid/upload")
    @ApiDoc("Upload file")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void uploadDocuments(HttpServerRequest request) {
        request.pause();
        UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(Renders.getHost(request), user.getUserId())
                        .compose(userSession -> {
                            request.resume();
                            return documentsService.uploadStreamedMultipleFiles(Field.FILECOUNT, request, userSession, vertx);
                        })
                        .onSuccess(res -> {
                            renderJson(request, res);
                            eventHelper.onCreateResource(request, RESOURCE_DOC);
                        })
                        .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()))));

    }

    @Put("/files/user/:userid/move/workspace")
    @ApiDoc("Move a file from Nextcloud to ENT workspace")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void moveToWorkspace(HttpServerRequest request) {
        List<String> listFiles = request.params().getAll(Field.PATH);
        String parentId = request.params().get(Field.PARENTID);
        if (Boolean.FALSE.equals(listFiles.isEmpty()))
            UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(Renders.getHost(request), user.getUserId())
                        .compose(userSession -> documentsService.moveDocumentToWorkspace(Renders.getHost(request), userSession, user, listFiles, parentId))
                        .onSuccess(res -> renderJson(request, new JsonObject().put(Field.DATA, res)))
                        .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()))));
        else
            badRequest(request);
    }

    @Put("/files/user/:userid/copy/workspace")
    @ApiDoc("Copy a file from Nextcloud to ENT workspace")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void copyToWorkspace(HttpServerRequest request) {
        List<String> listFiles = request.params().getAll(Field.PATH);
        String parentId = request.params().get(Field.PARENTID);
        // Defaults keep the historical behaviour: documents land in the user's own documents.
        String application = request.params().contains(Field.APPLICATION)
                ? request.params().get(Field.APPLICATION) : Field.APP;
        boolean protectedContent = Boolean.parseBoolean(request.params().get(Field.PROTECTED));
        if (!listFiles.isEmpty())
            UserUtils.getUserInfos(eb, request, user ->
                userService.getUserSession(Renders.getHost(request), user.getUserId())
                        .compose(userSession -> documentsService.copyDocumentToWorkspace(Renders.getHost(request), userSession, user, listFiles, parentId, application, protectedContent))
                        .onSuccess(res -> renderJson(request, new JsonObject().put(Field.DATA, res)))
                        .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()))));
        else
            badRequest(request);
    }

    @Put("/files/user/:userid/workspace/move/cloud")
    @ApiDoc("Move a file from ENT workspace to cloud")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void moveToCloud(HttpServerRequest request) {
        List<String> listFiles = request.params().getAll(Field.ID);
        String parentId = request.params().get(Field.PARENTNAME);
        if (!listFiles.isEmpty())
            UserUtils.getUserInfos(eb, request, user ->
                    userService.getUserSession(Renders.getHost(request), user.getUserId())
                            .compose(userSession -> documentsService.moveDocumentsFromWorkspaceToNC(Renders.getHost(request), userSession, user, listFiles, parentId))
                            .onSuccess(res -> {
                                renderJson(request, res);
                                eventHelper.onCreateResource(request, RESOURCE_DOC);
                            })
                            .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()))));
        else
            badRequest(request);
    }

    @Put("/files/user/:userid/workspace/copy/cloud")
    @ApiDoc("Copy a file from ENT workspace to cloud")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void copyToCloud(HttpServerRequest request) {
        List<String> listFiles = request.params().getAll(Field.ID);
        String parentId = request.params().get(Field.PARENTNAME);
        if (!listFiles.isEmpty())
            UserUtils.getUserInfos(eb, request, user ->
                    userService.getUserSession(Renders.getHost(request), user.getUserId())
                            .compose(userSession -> documentsService.copyDocumentsFromWorkspaceToNC(Renders.getHost(request), userSession, user, listFiles, parentId))
                            .onSuccess(res -> renderJson(request, res))
                            .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()))));
        else
            badRequest(request);
    }

    @Post("/files/user/:userid/create/folder")
    @ApiDoc("Create a folder in Nextcloud")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(OwnerFilter.class)
    public void createNewFolder(HttpServerRequest request) {
        String path = request.params().get(Field.PATH);
        if (!path.isEmpty())
            UserUtils.getUserInfos(eb, request, user ->
                    userService.getUserSession(Renders.getHost(request), user.getUserId())
                            .compose(userSession -> documentsService.createFolderNextcloud(Renders.getHost(request), userSession, path))
                            .onSuccess(res -> {
                                renderJson(request, res);
                                eventHelper.onCreateResource(request, RESOURCE_FOLDER);
                            })
                            .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()))));
        else
            badRequest(request);
    }

    private void initializeEventBusConsumers() {
        eventBus.consumer(BUS_ADDRESS, this::onBusMessage);
    }

    private void onBusMessage(Message<JsonObject> message) {
        String action = message.body().getString(Field.ACTION, "");
        if (COPY_FROM_STORAGE_ACTION.equals(action)) {
            copyFromStorage(message);
        } else {
            message.fail(400, "Unknown action: " + action);
        }
    }

    /**
     * Copy files of the ENT storage into the Nextcloud of a user. The calling module must have checked
     * that this user can access these files.
     * Body: { userId, host, path (optional), files: [{ fileId, metadata: { filename, content-type, size } }] }
     */
    private void copyFromStorage(Message<JsonObject> message) {
        JsonObject body = message.body();
        String userId = body.getString(Field.USERID_CAPS);
        String host = body.getString(Field.HOST);
        List<Attachment> files = body.getJsonArray(Field.FILES, new JsonArray()).stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(file -> new Attachment(file.getString(Field.FILEID), new Metadata(file.getJsonObject(Field.METADATA, new JsonObject()))))
                .collect(Collectors.toList());
        boolean hasInvalidFile = files.stream()
                .anyMatch(file -> file.id() == null || !StringHelper.isValidFileName(file.metadata().filename()));
        if (userId == null || host == null || files.isEmpty() || hasInvalidFile) {
            message.fail(400, "userId, host and files with a fileId and a valid metadata.filename are required");
            return;
        }
        String path = body.getString(Field.PATH);
        if (!StringHelper.isInsideUserRoot(path)) {
            message.fail(400, PATH_INVALID);
            return;
        }

        userService.getUserSession(host, userId)
                .onFailure(err -> message.fail(500, err.getMessage()))
                .onSuccess(userSession -> {
                    if (!userSession.hasOAuthAccessToken() && userSession.token() == null) {
                        message.fail(401, SESSION_MISSING);
                        return;
                    }
                    documentsService.copyStorageFilesToNC(host, userSession, files, path)
                            .onSuccess(results -> message.reply(new JsonObject()
                                    .put(Field.STATUS, Field.OK_LOWER)
                                    .put(Field.DATA, results)))
                            .onFailure(err -> message.fail(500, err.getMessage()));
                });
    }
}
