package fr.openent.nextcloud.service.impl;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.core.enums.NextcloudHttpMethod;
import fr.openent.nextcloud.core.enums.XmlnsAttr;
import fr.openent.nextcloud.helper.DocumentHelper;
import fr.openent.nextcloud.helper.HttpResponseHelper;
import fr.openent.nextcloud.helper.PromiseHelper;
import fr.openent.nextcloud.helper.XMLHelper;
import fr.openent.nextcloud.model.Document;
import fr.openent.nextcloud.model.XmlnsOptions;
import fr.openent.nextcloud.service.DocumentsService;
import fr.openent.nextcloud.service.ServiceFactory;
import fr.openent.nextcloud.service.UserService;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

public class DefaultDocumentsService implements DocumentsService {
    private final Logger log = LoggerFactory.getLogger(DefaultDocumentsService.class);
    private final WebClient client;
    private final NextcloudConfig nextcloudConfig;
    private final UserService userService;

    private static final String DOWNLOAD_ENDPOINT = "/index.php/apps/files/ajax/download.php";


    public DefaultDocumentsService(ServiceFactory serviceFactory) {
        this.client = serviceFactory.webClient();
        this.nextcloudConfig = serviceFactory.nextcloudConfig();
        this.userService = serviceFactory.userService();
    }
    
    /**
     * List files/folder
     *
     * @param userId User identifier
     * @param path   path of nextcloud's user
     * @return Future Instance of User from Nextcloud {@link JsonArray}
     */
    @Override
    public Future<JsonArray> listFiles(String userId, String userNextcloudId, String path) {
        Promise<JsonArray> promise = Promise.promise();
        userService.getUserSession(userId)
                .onSuccess(userSession ->
                        this.client.rawAbs(NextcloudHttpMethod.PROPFIND.method(), nextcloudConfig.host() +
                                        nextcloudConfig.webdavEndpoint() + "/" + userNextcloudId + (path != null ? "/" + path : "" ))
                                .basicAuthentication(userSession.loginName(), userSession.token())
                                .as(BodyCodec.string(StandardCharsets.UTF_8.toString()))
                                .sendBuffer(Buffer.buffer(getListFilesPropsBody()), responseAsync -> proceedListFiles(responseAsync, promise)))
                .onFailure(promise::fail);
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
            if (response.statusCode() != 207) {
                String messageToFormat = "[Nextcloud@%s::listFiles] Response status is not a HTTP 207 : %s : %s";
                HttpResponseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), response, promise);
            } else {
                JsonObject results = XMLHelper.toJsonObject(response.body());
                JsonArray responses;
                try {
                    responses = results.getJsonObject(Field.D_MULTISTATUS, new JsonObject()).getJsonArray(Field.D_RESPONSE, new JsonArray());
                } catch(ClassCastException e) {
                    String message = String.format("[Nextcloud@%s::proceedListFiles] An error has occurred during attempting to fetch response data : %s, " +
                            "returning empty list", this.getClass().getSimpleName(), e.getMessage());
                    log.error(message);
                    responses = new JsonArray();
                }
                List<Document> documents = DocumentHelper.documents(responses);
                promise.complete(new JsonArray(DocumentHelper.toListJsonObject(documents).toString()));
            }
        }
    }

    @Override
    public Future<Buffer> getFile(String userId, String userNextcloudId, String path) {
        Promise<Buffer> promise = Promise.promise();
        userService.getUserSession(userId)
                .onSuccess(userSession ->
                        this.client.getAbs(nextcloudConfig.host() + nextcloudConfig.webdavEndpoint() + "/" +
                                        userNextcloudId + (path != null ? "/" + path : "" ))
                                .basicAuthentication(userSession.loginName(), userSession.token())
                                .send(responseAsync -> proceedGetFile(responseAsync, promise)))
                .onFailure(promise::fail);
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
    public Future<Buffer> getFiles(String userId, String path, List<String> files) {
        Promise<Buffer> promise = Promise.promise();
        userService.getUserSession(userId)
                .onSuccess(userSession ->
                        this.client.getAbs(nextcloudConfig.host() + DOWNLOAD_ENDPOINT)
                                .basicAuthentication(userSession.loginName(), userSession.token())
                                .addQueryParam(Field.DIR, path)
                                .addQueryParam(Field.FILES, new JsonArray(files).toString())
                                .send(responseAsync -> proceedGetFile(responseAsync, promise)))
                .onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<JsonObject> uploadFile(String userId, String path) {
        Promise<JsonObject> promise = Promise.promise();
        this.client.put(NextcloudHttpMethod.PROPFIND.method(), nextcloudConfig.host() +
                        nextcloudConfig.webdavEndpoint() + "/" + userId + (path != null ? "/" + path : "" ))
                .basicAuthentication(this.nextcloudConfig.username(), this.nextcloudConfig.password())
                .as(BodyCodec.string(StandardCharsets.UTF_8.toString()))
                .sendBuffer(Buffer.buffer(""), responseAsync -> {
                    if (responseAsync.failed()) {
                        // on error
                    } else {
                        // on success
                    }
                });
        return promise.future();
    }

}
