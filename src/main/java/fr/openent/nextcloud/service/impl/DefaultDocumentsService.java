package fr.openent.nextcloud.service.impl;
import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.core.enums.NextcloudHttpMethod;
import fr.openent.nextcloud.core.enums.XmlnsAttr;
import fr.openent.nextcloud.helper.*;
import fr.openent.nextcloud.model.Document;
import fr.openent.nextcloud.model.UserNextcloud;
import fr.openent.nextcloud.model.XmlnsOptions;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.entcore.common.bus.WorkspaceHelper;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserInfos;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DefaultDocumentsService implements DocumentsService {
    private final Logger log = LoggerFactory.getLogger(DefaultDocumentsService.class);
    private final WebClient client;
    private final NextcloudConfig nextcloudConfig;
    private final Storage storage;
    private final WorkspaceHelper workspaceHelper;
    private final EventBus eventBus;

    private static final String DOWNLOAD_ENDPOINT = "/index.php/apps/files/ajax/download.php";


    public DefaultDocumentsService(ServiceFactory serviceFactory) {
        this.client = serviceFactory.webClient();
        this.nextcloudConfig = serviceFactory.nextcloudConfig();
        this.storage = serviceFactory.storage();
        this.workspaceHelper = serviceFactory.workspaceHelper();
        this.eventBus = serviceFactory.eventBus();
    }
    
    /**
     * List files/folder
     *
     * @param userSession   User Session
     * @param path   path of nextcloud's user
     * @return Future Instance of User from Nextcloud {@link JsonArray}
     */
    @Override
    public Future<JsonArray> listFiles(UserNextcloud.TokenProvider userSession, String path) {
        Promise<JsonArray> promise = Promise.promise();
        this.client.rawAbs(NextcloudHttpMethod.PROPFIND.method(), nextcloudConfig.host() +
                        nextcloudConfig.webdavEndpoint() + "/" + userSession.userId() + (path != null ? "/" + path.replace(" ", "%20") : "" ))
                .basicAuthentication(userSession.userId(), userSession.token())
                .as(BodyCodec.string(StandardCharsets.UTF_8.toString()))
                .sendBuffer(Buffer.buffer(getListFilesPropsBody()), responseAsync -> proceedListFiles(responseAsync, promise));
        return promise.future();
    }

    /**
     * prepare PROPFIND body request (sent as XML stringify)
     *
     * @return XML as body string to process PROPFIND
     */
    private String getListFilesPropsBody() {
        Document.RequestBody requestBody = new Document.RequestBody();
        XmlnsOptions xmlnsOptions = new XmlnsOptions()
                .setWebDavTag(XmlnsAttr.D)
                .setNextcloudTag(XmlnsAttr.NC)
                .setOwnCloudTag(XmlnsAttr.OC);
        return XMLHelper.createXML(requestBody.toJSON(), Field.D_PROPFIND, xmlnsOptions);
    }

    /**
     * Proceed async event after HTTP PROPFIND (fetching files/folder) API endpoint has been sent
     *
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link JsonArray}
     */
    private void proceedListFiles(AsyncResult<HttpResponse<String>> responseAsync, Promise<JsonArray> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::listFiles] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<String> response = responseAsync.result();
            // complete promise if 207.
            // Even if we fetch 404 status, as we consider the request has been fulfilled but has not found the document(s)
            // we still attempt to create an array (will be an empty one)
            if (response.statusCode() == 207 || response.statusCode() == 404) {
                JsonObject results = XMLHelper.toJsonObject(response.body());
                JsonArray responses = getResultMultiStatus(results);
                List<Document> documents = DocumentHelper.documents(responses);
                promise.complete(new JsonArray(DocumentHelper.toListJsonObject(documents).toString()));
            } else {
                String messageToFormat = "[Nextcloud@%s::listFiles] Response status is not a HTTP 207 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            }
        }
    }

    /**
     * method that will determine result multi status
     *
     * @param   results   results documents to parse
     * @return  list of response documents
     */
    private JsonArray getResultMultiStatus(JsonObject results) {
        JsonArray responses;
        try {
            JsonObject multiStatusObject = results.getJsonObject(Field.D_MULTISTATUS, new JsonObject());
            // if fetching D_RESPONSE value is JsonArray, it's an array of response, else it would be a JsonObject one
            if (multiStatusObject.getValue(Field.D_RESPONSE) instanceof JsonArray) {
                responses = multiStatusObject.getJsonArray(Field.D_RESPONSE, new JsonArray());
            } else {
                responses = new JsonArray();
                if (!multiStatusObject.getJsonObject(Field.D_RESPONSE, new JsonObject()).isEmpty()) {
                    responses.add(multiStatusObject.getJsonObject(Field.D_RESPONSE, new JsonObject()));
                }
            }
        } catch (ClassCastException e) {
            String message = String.format("[Nextcloud@%s::proceedListFiles] An error has occurred during attempting to fetch response data : %s, " +
                    "returning empty list", this.getClass().getSimpleName(), e.getMessage());
            log.error(message);
            responses = new JsonArray();
        }
        return responses;
    }

    @Override
    public Future<HttpResponse<Buffer>> getFile(UserNextcloud.TokenProvider userSession, String path) {
        Promise<HttpResponse<Buffer>> promise = Promise.promise();
        this.client.getAbs(nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" +
                        userSession.userId() + (path != null ? "/" + path : "" ))
                .basicAuthentication(userSession.userId(), userSession.token())
                .send(responseAsync -> proceedGetFile(responseAsync, promise));
        return promise.future();
    }

    /**
     * Proceed async event after HTTP get (get/downloading) file API endpoint has been sent
     *
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link Buffer}
     */
    private void proceedGetFile(AsyncResult<HttpResponse<Buffer>> responseAsync, Promise<HttpResponse<Buffer>> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::proceedGetFile] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<Buffer> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String messageToFormat = "[Nextcloud@%s::proceedGetFile] Response status is not a HTTP 200 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                promise.complete(response);
            }
        }
    }

    @Override
    public Future<HttpResponse<Buffer>> getFiles(UserNextcloud.TokenProvider userSession, String path, List<String> files) {
        Promise<HttpResponse<Buffer>> promise = Promise.promise();
        this.client.getAbs(nextcloudConfig.host() + DOWNLOAD_ENDPOINT)
                .basicAuthentication(userSession.userId(), userSession.token())
                .addQueryParam(Field.DIR, path)
                .addQueryParam(Field.FILES, new JsonArray(files).toString())
                .send(responseAsync -> proceedGetFile(responseAsync, promise));
        return promise.future();
    }

    @Override
    public Future<JsonObject> moveDocument(UserNextcloud.TokenProvider userSession, String path, String destPath) {
        Promise<JsonObject> promise = Promise.promise();
        String endpoint = nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" + userSession.userId();
        this.listFiles(userSession, destPath)
                .onSuccess(files -> {
                    if (files.isEmpty()) {
                        this.client.rawAbs(NextcloudHttpMethod.MOVE.method(), endpoint + "/" + path)
                                .basicAuthentication(userSession.userId(), userSession.token())
                                .putHeader(Field.DESTINATION, endpoint + "/" + destPath)
                                .send(responseAsync -> this.onMoveDocumentHandler(responseAsync, promise));
                    } else {
                        promise.fail("nextcloud.file.already.exist");
                    }
                })
                .onFailure(promise::fail);
        return promise.future();
    }


    /**
     * Proceed async event after HTTP MOVE documents API endpoint has been sent
     *
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link JsonObject}
     */
    private void onMoveDocumentHandler(AsyncResult<HttpResponse<Buffer>> responseAsync, Promise<JsonObject> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::onMoveDocumentHandler] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<Buffer> response = responseAsync.result();
            if (response.statusCode() != 201) {
                String messageToFormat = "[Nextcloud@%s::onMoveDocumentHandler] Response status is not a HTTP 201 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
            }
        }
    }

    @Override
    public Future<JsonObject> deleteDocuments(UserNextcloud.TokenProvider userSession, List<String> paths) {
        Promise<JsonObject> promise = Promise.promise();

        Future<Void> current = Future.succeededFuture();

        for (String path : paths) {
            current = current.compose(v -> this.deleteDocument(userSession, path));
        }
        current
                .onSuccess(res -> promise.complete(new JsonObject().put(Field.STATUS, Field.OK)))
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::deleteDocuments] An error has occurred during deleting document(s): %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });
        return promise.future();
    }

    /**
     * method that delete document / path of folder within document(s)
     *
     * @param   userSession     User Session {@link UserNextcloud.TokenProvider}
     * @param   path            path to delete
     */
    private Future<Void> deleteDocument(UserNextcloud.TokenProvider userSession, String path) {
        Promise<Void> promise = Promise.promise();
        this.client.deleteAbs(nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" + userSession.userId() + "/" + path)
                .basicAuthentication(userSession.userId(), userSession.token())
                .send(responseAsync -> onDeleteDocumentHandler(responseAsync, promise));
        return promise.future();
    }

    /**
     * Proceed async event after HTTP DELETE document API endpoint has been sent
     *
     * @param   responseAsync   HttpResponse of string depending on its state {@link AsyncResult}
     * @param   promise         Promise that could be completed or fail sending {@link JsonObject}
     */
    private void onDeleteDocumentHandler(AsyncResult<HttpResponse<Buffer>> responseAsync, Promise<Void> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::onDeleteDocumentHandler] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<Buffer> response = responseAsync.result();
            if (response.statusCode() != 204) {
                String messageToFormat = "[Nextcloud@%s::onDeleteDocumentHandler] Response status is not a HTTP 204 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                promise.complete();
            }
        }
    }

    /**
     * Upload multiple files to the nextcloud
     * @param userSession      User session
     * @param files            List of files to upload
     * @param path             Final path on Nextcloud
     * @return                 Future JsonArray with data on the uploaded files
     */
    @Override
    public Future<JsonArray> uploadFiles(UserNextcloud.TokenProvider userSession, List<Attachment> files, String path) {
        Promise<JsonArray> promise = Promise.promise();
        Future<JsonObject> current = Future.succeededFuture();
        JsonArray stateUploadedFiles = new JsonArray();
        for (Attachment file : files) {
            current = current.compose(res -> {
                if (res != null) {
                    stateUploadedFiles.add(res);
                }
                return this.uploadFile(userSession, file, path);
            });
        }
        current
                .onSuccess(res -> {
                    stateUploadedFiles.add(res);
                    promise.complete(stateUploadedFiles);
                })
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::uploadFiles] An error has occurred during uploading files : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });
        return promise.future();
    }

    /**
     * Copy all the files listed in the filesPath from nextcloud to workspace.
     * @param userSession       User session
     * @param user              User infos
     * @param filesPath         Path of all the files to move
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future Json with the infos about every copy
     */
    public Future<JsonObject> copyDocumentToWorkspace(UserNextcloud.TokenProvider userSession,
                                                      UserInfos user,
                                                      List<String> filesPath,
                                                      String parentId) {
        Promise<JsonObject> promise = Promise.promise();
        Future<JsonObject> current = Future.succeededFuture();

        JsonObject result = new JsonObject();
        for (String file : filesPath) {
            current = current.compose(v -> copyToWorkspace(userSession, user, file, result, parentId));
        }
        current.onSuccess(res -> promise.complete(result))
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::copyDocumentToWorkspace] An error has occurred during copy : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });

        return promise.future();
    }

    /**
     * Move all the files listed in the filesPath from nextcloud to workspace.
     * @param userSession       User session
     * @param user              User infos
     * @param filesPath         Path of all the files to move
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future Json with infos about every move
     */
    @Override
    public Future<JsonObject> moveDocumentToWorkspace(UserNextcloud.TokenProvider userSession,
                                                      UserInfos user,
                                                      List<String> filesPath,
                                                      String parentId) {
        Promise<JsonObject> promise = Promise.promise();
        Future<JsonObject> current = Future.succeededFuture();
        JsonObject result = new JsonObject();
        for (String file : filesPath) {
            current = current.compose(v -> moveToWorkspace(userSession, user, file, result, parentId));
        }
        current.onSuccess(res -> promise.complete(result))
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::moveDocumentToWorkspace] An error has occurred while moving documents : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });
        return promise.future();
    }

    /** Copy one document from Nextcloud to Workspace
     * @param userSession       User session
     * @param user              User infos
     * @param file              Path of the file you want to move
     * @param result            Json where status of move is stored
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future Json with the infos about the copy
     */
    private Future<JsonObject> copyToWorkspace(UserNextcloud.TokenProvider userSession,
                                               UserInfos user,
                                               String file,
                                               JsonObject result,
                                               String parentId) {
        Promise<JsonObject> promiseResult = Promise.promise();
        //The listFiles function here is called to gather data on one specific file.
        listFiles(userSession, file)
                .onSuccess(fileInfo -> {
                    if (!fileInfo.isEmpty()) {
                        JsonObject resJson = fileInfo.getJsonObject(0);
                        if (!resJson.containsKey(Field.DISPLAYNAME) || resJson.getString(Field.DISPLAYNAME).equals("")
                                || !resJson.containsKey(Field.ISFOLDER)) {
                            String messageToFormat = "[Nextcloud@%s::copyToWorkspace] An error has occurred while retrieving data from file : %s";
                            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("Missing data field in listFiles infos"), promiseResult);
                            return;
                        }
                        if (Boolean.FALSE.equals(fileInfo.getJsonObject(0).getBoolean(Field.ISFOLDER))) {
                            storeFileWorkspace(userSession, user, fileInfo.getJsonObject(0).getString(Field.DISPLAYNAME), parentId).onComplete(status -> {
                                if (status.succeeded()) {
                                    result.put(file, status.result());
                                }
                                else {
                                    result.put(file, status.cause().getMessage());
                                }
                                promiseResult.complete();
                            });
                        } else {
                            //TODO Gérer le cas d'import d'un dossier
                            log.error("[Nextcloud@%s::copyToWorkspace] import.directory.not.handled");
                            result.put(file, "import.directory.not.handled");
                            promiseResult.complete();
                        }
                    } else {
                        result.put(file, "nextcloud.server.file.no.exist");
                        promiseResult.complete();
                    }

                })
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::copyToWorkspace] An error has occurred while retrieving data from file : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promiseResult);
                });
        return promiseResult.future();

    }

    /** Move one document from Nextcloud to Workspace
     * @param userSession       User session
     * @param user              User infos
     * @param file              Path of the file you want to move
     * @param result            Json where status of move is stored
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future Json with the infos about the move
     */
    private Future<JsonObject> moveToWorkspace(UserNextcloud.TokenProvider userSession,
                                               UserInfos user,
                                               String file,
                                               JsonObject result,
                                               String parentId) {
        Promise<JsonObject> promiseResult = Promise.promise();
        //The listFiles function here is called to gather data on one specific file.
        listFiles(userSession, file)
                .onSuccess(fileInfo -> {
                    if (!fileInfo.isEmpty()) {
                        JsonObject resJson = fileInfo.getJsonObject(0);

                        if (!resJson.containsKey(Field.DISPLAYNAME) || resJson.getString(Field.DISPLAYNAME).equals("")
                                || !resJson.containsKey(Field.ISFOLDER)) {
                            String messageToFormat = "[Nextcloud@%s::moveToWorkspace] An error has occurred while retrieving data from file : %s";
                            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("Missing data field in listFiles infos"), promiseResult);
                            return;
                        }
                        if (Boolean.FALSE.equals(fileInfo.getJsonObject(0).getBoolean(Field.ISFOLDER))) {
                            retrieveAndDeleteFile(userSession, user, fileInfo.getJsonObject(0).getString(Field.DISPLAYNAME), parentId)
                                    .onComplete(status -> {
                                if (status.succeeded()) {
                                    result.put(file, status.result());
                                } else {
                                    result.put(file, status.cause().getMessage());
                                }
                                promiseResult.complete();
                            });
                        } else {
                            //TODO Gérer le cas d'import d'un dossier
                            log.error("[Nextcloud@%s::moveToWorkspace] import.directory.not.handled");
                            result.put(file, "import.directory.not.handled");
                            promiseResult.complete();
                        }
                    } else {
                        result.put(file, "nextcloud.server.file.no.exist");
                        promiseResult.complete();
                    }
                })
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::moveToWorkspace] An error has occurred while retrieving data from file : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promiseResult);
                });
        return promiseResult.future();
    }



    /**
     * Copy a file from Nextcloud server to workspace and then delete it from nextcloud
     * If the storage succeed, delete the document from nextcloud, otherwise return a failed future.
     * @param userSession       User session
     * @param user              User infos
     * @param filePath          Path of the file on nextcloud server
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future with the status of the action
     */
    private Future<JsonObject> retrieveAndDeleteFile(UserNextcloud.TokenProvider userSession, UserInfos user, String filePath, String parentId) {
        Promise<JsonObject> promise = Promise.promise();
        storeFileWorkspace(userSession, user, filePath, parentId)
                .onSuccess(res -> {
                    promise.complete(res);
                    deleteDocument(userSession, filePath);
                })
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::retrieveAndDeleteFile] An error has occurred during retrieving" + "" +
                            "and deleting document document(s) : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });
        return promise.future();
    }

    /**
     * Retrieve file from user's Nextcloud space and store it in his ENT workspace
     * @param userSession       User session
     * @param user              User infos
     * @param filePath          Path of the file on nextcloud server
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future with the status of the action
     */
    private Future<JsonObject> storeFileWorkspace(UserNextcloud.TokenProvider userSession, UserInfos user, String filePath, String parentId) {
        Promise<JsonObject> promise = Promise.promise();

        getFile(userSession, filePath)
                .compose(buffer ->
                        FileHelper.writeBuffer(storage, buffer.bodyAsBuffer(), buffer.headers().get(Field.CONTENT_TYPE_HEADER), filePath)
                )
                .compose(writeInfo -> {
                    writeInfo.put(Field.PARENTID, parentId);
                    return FileHelper.addFileReference(writeInfo, user, filePath.replace("%20", " "), workspaceHelper);
                })
                .compose(resDoc -> moveUnderParent(workspaceHelper, user, parentId, resDoc))
                .onSuccess(promise::complete)
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::storeFileWorkspace] Error while storing file : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });

        return promise.future();
    }

    /**
     * Check if the new file need to be moved under a parent folder and do it if the answer is yes
     * @param user          User infos
     * @param parentId      Identifier of the previous folder if moving in a folder
     * @return              Future with the status of the action
     */
    private Future<JsonObject> moveUnderParent(WorkspaceHelper workspaceHelper, UserInfos user, String parentId, JsonObject resDoc) {
        Promise<JsonObject> promise = Promise.promise();
        if (parentId != null) {
            if (!resDoc.containsKey(Field.UNDERSCORE_ID)) {
                String messageToFormat = "[Nextcloud@%s::moveUnderParent] An error has occurred during moving document(s) : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("Missing return ID"), promise);
                return promise.future();
            }
            moveDocumentUnderParent(workspaceHelper, resDoc.getString(Field.UNDERSCORE_ID), parentId, user)
                    .onSuccess(promise::complete)
                    .onFailure(res -> {
                        String messageToFormat = "[Nextcloud@%s::moveUnderParent] An error has occurred during moving document(s) : %s";
                        PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), res, promise);
                    });
        } else {
            promise.complete(new JsonObject().put(Field.STATUS, resDoc.getString(Field.STATUS)));
        }
        return promise.future();
    }

    /**
     * Move a file under a folder in the workspace, the file and the folder need to exist
     * @param workspaceHelper   Help to manage properly workspace
     * @param id                File identifier
     * @param parentId          Parent folder Identifier
     * @param user              User infos
     * @return                  Future with the status of the move
     */
    private Future<JsonObject> moveDocumentUnderParent(WorkspaceHelper workspaceHelper, String id, String parentId, UserInfos user) {
        Promise<JsonObject> promise = Promise.promise();
        workspaceHelper.moveDocument(id, parentId, user, moveStatus -> {
                    if (moveStatus.failed()) {
                        String messageToFormat = "[Nextcloud@%s::moveDocumentUnderParent] Error while moving document under parent folder : %s";
                        PromiseHelper.reject(log, messageToFormat, FileHelper.class.getName(), new Exception("parent.folder.not.exist"), promise);
                    } else
                        promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
                });
        return promise.future();
    }

    /**
     * upload file
     *  @param user         User session token
     *  @param file         Data about the file to upload
     *  @param path         Path where files will be uploaded on the nextcloud
     */
    @Override
    public Future<JsonObject> uploadFile(UserNextcloud.TokenProvider user, Attachment file, String path) {
        //Final path on the nextcloud server
        String finalPath = (path != null ? path + "/" : "" ) + file.metadata().filename();
        Promise<JsonObject> promise = Promise.promise();
        this.listFiles(user, finalPath) //check if the file currently exists on the nextcloud server
                .onSuccess(files -> {
                    if (files.isEmpty()) {
                        //Read the file on the vertx container, id is needed to locate it
                        storage.readFile(file.id(), res ->
                            this.client.putAbs(nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" + user.userId() + "/" + finalPath.replace(" ", "%20"))
                                    .basicAuthentication(user.userId(), user.token())
                                    .as(BodyCodec.jsonObject())
                                    .sendBuffer(res, responseAsync -> {
                                        if (responseAsync.failed()) {
                                            String messageToFormat = "[Nextcloud@%s::uploadFile] An error has occurred during uploading file : %s";
                                            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync.cause(), promise);
                                        } else {
                                            promise.complete(new JsonObject()
                                                    .put(Field.NAME, file.metadata().filename())
                                                    .put(Field.STATUSCODE, responseAsync.result().statusCode()));
                                        }
                                    }));

                    } else {
                        promise.complete(new JsonObject()
                                .put(Field.NAME, file.metadata().filename())
                                .put(Field.ERROR, "nextcloud.file.already.exist"));
                    }
                    storage.removeFile(file.id(), e -> {});
                })
                .onFailure(err -> {
                    promise.complete(new JsonObject().put(Field.ERROR, err));
                    storage.removeFile(file.id(), e -> {});
                });
        return promise.future();
    }

    /**
     * Move all the files listed in id idList from workspace to Nextcloud
     * @param userSession   User session
     * @param user          User infos
     * @param idList        Identifier of all the files to move
     * @param parentName    Name of the parent folder in nextcloud
     * @return              Future Json with all the status infos about the move.
     */
    @Override
    public Future<JsonObject> moveFilesFromWorkspaceToNC(UserNextcloud.TokenProvider userSession, UserInfos user, List<String> idList, String parentName) {
        Promise<JsonObject> promise = Promise.promise();

        Future<Void> current = Future.succeededFuture();
        JsonObject result = new JsonObject();
        for (String id : idList) {
            current = current.compose(v -> moveFromWorkspaceToNC(userSession, id, parentName, result));
        }
        current.onSuccess(res -> promise.complete(result))
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::moveFilesFromWorkspaceToNC] An error has occurred while moving documents : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });

        return promise.future();
    }

    /**
     * Move one file from workspace to Nextcloud
     * @param userSession   User session
     * @param id            Identifier of the file
     * @param parentName    Name of the parent folder in Nextcloud
     * @param result        JsonObject containing all the status about the move
     * @return              Future Json with status of the move.
     */
    private Future<Void> moveFromWorkspaceToNC(UserNextcloud.TokenProvider userSession, String id, String parentName, JsonObject result) {
        Promise<Void> promise = Promise.promise();

        sendWorkspaceFileToNC(userSession, id, parentName)
                .compose(deleteStatus -> {
                    result.put(id, deleteStatus);
                    return EventBusHelper.deleteDocument(eventBus, id, userSession.userId());
                })
                .onSuccess(res -> promise.complete())
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::moveFromWorkspaceToNC] An error has occurred while moving file : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });

        return promise.future();
    }

    /**
     * Retrieve a file from workspace and send it to Nextcloud.
     * @param userSession   User session
     * @param id            Identifier of the file
     * @param parentName    Name of the parent folder in Nextcloud
     * @return              Future Json with result of the upload
     */
    private Future<JsonObject> sendWorkspaceFileToNC(UserNextcloud.TokenProvider userSession, String id, String parentName) {
        Promise<JsonObject> promise = Promise.promise();
        String finalPath = (parentName != null ? parentName + "/" : "" );

        workspaceHelper.readDocument(id, file -> {
            if (file != null) {
                String docName = file.getDocument().getString(Field.NAME).replace(" ", "%20");
                this.client.putAbs(nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" + userSession.userId() + "/" +
                                finalPath + docName)
                        .basicAuthentication(userSession.userId(), userSession.token())
                        .sendBuffer(file.getData(), responseAsync -> {
                            if (responseAsync.failed()) {
                                String messageToFormat = "[Nextcloud@%s::sendWorkspaceFileToNC] An error has occurred during uploading file : %s";
                                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync.cause(), promise);
                            } else {
                                promise.complete(new JsonObject().put(Field.STATUSCODE, responseAsync.result().statusCode()));
                            }
                        });
            } else {
                String messageToFormat = "[Nextcloud@%s::sendWorkspaceFileToNC] An error has occurred during uploading file : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("file.not.found"), promise);
            }
        });

        return promise.future();
    }




}
