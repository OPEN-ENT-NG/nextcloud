package fr.openent.nextcloud.service;

import fr.openent.nextcloud.helper.Attachment;
import fr.openent.nextcloud.model.UserNextcloud;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.storage.Storage;

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
    Future<Buffer> getFile(UserNextcloud.TokenProvider userSession, String path);

    /**
     * download multiple files (.zip given)
     *
     * @param userSession       User Session {@link UserNextcloud.TokenProvider}
     * @param path              path of nextcloud's user
     * @param files             List of wanted file to download
     * @return  Future containing Buffer of file {@link Buffer}
     */
    Future<Buffer> getFiles(UserNextcloud.TokenProvider userSession, String path, List<String> files);

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
     *  @param storage      Storage manager
     *  @param file         Data about the file to upload
     *  @param path         Path where files will be uploaded on the nextcloud
     */
    Future<JsonObject> uploadFile(UserNextcloud.TokenProvider user, Storage storage, Attachment file, String path);

    /**
     * upload files
     *  @param userSession      User session
     *  @param files            List of files to upload
     *  @param storage          Storage manager
     *  @param path             Final path on Nextcloud
     *  @return                 Future of uploaded files
     */
    Future<JsonArray> uploadFiles(UserNextcloud.TokenProvider userSession, List<Attachment> files, Storage storage, String path);
}
