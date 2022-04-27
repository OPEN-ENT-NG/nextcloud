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

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class DefaultDocumentsService implements DocumentsService {
    private final Logger log = LoggerFactory.getLogger(DefaultDocumentsService.class);
    private final WebClient client;
    private final NextcloudConfig nextcloudConfig;
    private final Storage storage;
    private final WorkspaceHelper workspaceHelper;

    private static final String DOWNLOAD_ENDPOINT = "/index.php/apps/files/ajax/download.php";


    public DefaultDocumentsService(ServiceFactory serviceFactory) {
        this.client = serviceFactory.webClient();
        this.nextcloudConfig = serviceFactory.nextcloudConfig();
        this.storage = serviceFactory.storage();
        this.workspaceHelper = serviceFactory.workspaceHelper();
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
                        nextcloudConfig.webdavEndpoint() + "/" + userSession.userId() + (path != null ? "/" + path : "" ))
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
     *
     * @param userSession       User session
     * @param filesPath         Path of all the files to move
     * @return                  Future of the move's state
     */
    @Override
    public Future<JsonObject> moveDocumentENT(UserNextcloud.TokenProvider userSession, UserInfos user, List<String> filesPath, String parentId) {
        Promise<JsonObject> promise = Promise.promise();
        Future<JsonObject> current = Future.succeededFuture();

        for (String file : filesPath) {
            current = current.compose(v -> moveLocal(userSession, user, file, parentId));
        }
        current.onSuccess(res -> promise.complete(new JsonObject().put(Field.STATUS, Field.OK)))
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::moveDocumentENT] An error has occurred during deleting document(s): %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });
        return promise.future();
    }

    private Future<JsonObject> moveLocal(UserNextcloud.TokenProvider userSession, UserInfos user, String filePath, String parentId) {
        return copyLocal(userSession, user, filePath, parentId)
                .onSuccess(res -> deleteDocument(userSession, filePath));
    }

    private Future<JsonObject> copyLocal(UserNextcloud.TokenProvider userSession, UserInfos user, String filePath, String parentId) {
        Promise<JsonObject> promise = Promise.promise();
        getFile(userSession, filePath)
                .onSuccess(buffer -> {
                    JsonObject uploaded = new JsonObject()
                            .put("parentId", parentId)
                            .put("_id", UUID.randomUUID().toString());
                    String fileName = Paths.get(filePath).toString();
                    storage.writeBuffer(uploaded.getString("_id"), buffer.bodyAsBuffer(), null, fileName, res -> {
                        workspaceHelper.addDocument(uploaded
                                        .put("metadata", res.getJsonObject("metadata")),
                                user,
                                fileName,
                                null,
                                false,
                                null,
                                e -> {}
                        );
                    });
                    promise.complete();
                })
                .onFailure(err -> {
                    String messageToFormat = "[Nextcloud@%s::moveLocal] An error has occurred while moving files : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
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
                            this.client.putAbs(nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" + user.userId() + "/" + finalPath)
                                    .basicAuthentication(this.nextcloudConfig.username(), this.nextcloudConfig.password())
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

}
