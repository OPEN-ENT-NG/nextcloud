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
import org.entcore.common.storage.Storage;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class DefaultDocumentsService implements DocumentsService {
    private final Logger log = LoggerFactory.getLogger(DefaultDocumentsService.class);
    private final WebClient client;
    private final NextcloudConfig nextcloudConfig;

    private static final String DOWNLOAD_ENDPOINT = "/index.php/apps/files/ajax/download.php";


    public DefaultDocumentsService(ServiceFactory serviceFactory) {
        this.client = serviceFactory.webClient();
        this.nextcloudConfig = serviceFactory.nextcloudConfig();
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
    public Future<Buffer> getFile(UserNextcloud.TokenProvider userSession, String path) {
        Promise<Buffer> promise = Promise.promise();
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
    private void proceedGetFile(AsyncResult<HttpResponse<Buffer>> responseAsync, Promise<Buffer> promise) {
        if (responseAsync.failed()) {
            String messageToFormat = "[Nextcloud@%s::proceedGetFile] An error has occurred during fetching endpoint : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), responseAsync, promise);
        } else {
            HttpResponse<Buffer> response = responseAsync.result();
            if (response.statusCode() != 200) {
                String messageToFormat = "[Nextcloud@%s::proceedGetFile] Response status is not a HTTP 200 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                promise.complete(response.body());
            }
        }
    }

    @Override
    public Future<Buffer> getFiles(UserNextcloud.TokenProvider userSession, String path, List<String> files) {
        Promise<Buffer> promise = Promise.promise();
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
//                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
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
     * @param storage          Storage manager
     * @param path             Final path on Nextcloud
     * @return                 Future JsonArray with data on the uploaded files
     */
    @Override
    public Future<JsonArray> uploadFiles(UserNextcloud.TokenProvider userSession, List<Attachment> files, Storage storage, String path) {
        Promise<JsonArray> promise = Promise.promise();
        Future<JsonObject> current = Future.succeededFuture();
        JsonArray answer = new JsonArray();
        for (Attachment file : files) {
            current = current.compose(v -> {
                if (v != null) {
                    answer.add(v);
                }
                return this.uploadFile(userSession, file.id(),
                        storage,
                        (path != null ? path + "/" : "" ) + file.metadata().filename()
                );
            });
        }
        current
                .onSuccess(res -> {
                    answer.add(res);
                    promise.complete(answer);
                })
                .onFailure(err -> {
                    log.error("[Nextcloud@%s::deleteDocuments] An error has occurred during uploading files");
                });
        return promise.future();
    }

    /**
     * Upload one file to the nextcloud server
     * @param user         User identifier
     * @param path         File's path in the vertx container
     * @param storage      Storage manager
     * @param filename     The name of the uploaded file
     * @return             Future JsonObject with data on the uploaded file
     */
    @Override
    public Future<JsonObject> uploadFile(UserNextcloud.TokenProvider user, String path, Storage storage, String filename) {
        Promise<JsonObject> promise = Promise.promise();
        this.listFiles(user, path)
                .onSuccess(files -> {
                    if (files.isEmpty()) {
                        storage.readFile(path, res -> {
                            this.client.putAbs(nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" + user.userId() + "/" + filename)
                                    .basicAuthentication(this.nextcloudConfig.username(), this.nextcloudConfig.password())
                                    .basicAuthentication(user.userId(), user.token())
                                    .as(BodyCodec.jsonObject())
                                    .sendBuffer(res, responseAsync -> {
                                        if (responseAsync.failed()) {
                                            promise.fail(new JsonObject("[Nextcloud@%s::uploadFile] An error has occurred during fetching endpoint").encode());
                                        } else {
                                            promise.complete(new JsonObject()
                                                    .put("name", filename)
                                                    .put("statusCode", responseAsync.result().statusCode()));
                                            storage.removeFile(path, err -> log.error("Deleting file" + path));
                                        }
                                    });
                        });

                    } else {
                        promise.complete(new JsonObject()
                                .put("name", path)
                                .put("error", "nextcloud.file.already.exist\""));
                    }
                })
                .onFailure(err -> promise.complete(new JsonObject().put("error", err)));

        return promise.future();
    }

}
