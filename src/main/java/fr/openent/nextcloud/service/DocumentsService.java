package fr.openent.nextcloud.service;

import fr.openent.nextcloud.helper.Attachment;
import fr.openent.nextcloud.model.UserNextcloud;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import org.entcore.common.bus.WorkspaceHelper;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserInfos;

import java.util.List;

public interface DocumentsService {

    /**
     * List files/folder
     *
     * @param userSession   User Session {@link UserNextcloud.TokenProvider}
     * @param path          path of nextcloud's user
     * @return  Future List of folder/list {@link JsonArray}
     */
    Future<JsonArray> listFiles(UserNextcloud.TokenProvider userSession, String path);

    /**
     * get/download file
     *
     * @param userSession   User Session {@link UserNextcloud.TokenProvider}
     * @param path          path of nextcloud's user
     * @return  Future containing Buffer of file {@link Buffer}
     */
    Future<HttpResponse<Buffer>> getFile(UserNextcloud.TokenProvider userSession, String path);

    /**
     * download multiple files (.zip given)
     *
     * @param userSession       User Session {@link UserNextcloud.TokenProvider}
     * @param path              path of nextcloud's user
     * @param files             List of wanted file to download
     * @return  Future containing Buffer of file {@link Buffer}
     */
    Future<HttpResponse<Buffer>> getFiles(UserNextcloud.TokenProvider userSession, String path, List<String> files);

    /**
     * Move Document
     *
     * @param userSession       User Session {@link UserNextcloud.TokenProvider}
     * @param path              path of nextcloud's user wanted to move
     * @param destPath          destination path
     * @return  Future JsonObject response of move document
     */
    Future<JsonObject> moveDocument(UserNextcloud.TokenProvider userSession, String path, String destPath);

    /**
     * delete documents
     *
     * @param userSession   User Session {@link UserNextcloud.TokenProvider}
     * @param paths         list of paths / documents to delete
     */
    Future<JsonObject> deleteDocuments(UserNextcloud.TokenProvider userSession, List<String> paths);

    /**
     * upload file
     *  @param user         User session token
     *  @param file         Data about the file to upload
     *  @param path         Path where files will be uploaded on the nextcloud
     */
    Future<JsonObject> uploadFile(UserNextcloud.TokenProvider user, Attachment file, String path);

    /**
     * upload files
     *  @param userSession      User session
     *  @param files            List of files to upload
     *  @param path             Final path on Nextcloud
     *  @return                 Future of uploaded files
     */
    Future<JsonArray> uploadFiles(UserNextcloud.TokenProvider userSession, List<Attachment> files, String path);

    /**
     *
     * @param userSession       User session
     * @param filesPath         Path of all the files to move
     * @return                  Future of the move's state
     */
    Future<JsonObject> moveDocumentENT(UserNextcloud.TokenProvider userSession, UserInfos user, List<String> filesPath,  String parentId);
}
