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

    public void parametrizedListFiles(UserNextcloud.TokenProvider userSession, String path, Handler<AsyncResult<HttpResponse<String>>> handler);

    /**
     * get/download file
     *
     * @param userSession   User Session {@link UserNextcloud.TokenProvider}
     * @param path          path of nextcloud's user
     * @return  Future containing Buffer of file {@link Buffer}
     */
    Future<HttpResponse<Buffer>> getFile(UserNextcloud.TokenProvider userSession, String path);

    /**
     * get/download folder
     *
     * @param userSession   User Session {@link UserNextcloud.TokenProvider}
     * @param path          folder path of nextcloud's user
     * @return  Future containing Buffer of folder {@link Buffer}
     */
    Future<HttpResponse<Buffer>> getFolder(UserNextcloud.TokenProvider userSession, String path);

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
     * Copy all the files listed in the filesPath from nextcloud to local.
     * @param userSession       User session
     * @param user              User infos
     * @param filesPath         Path of all the files to move
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future list of JsonObject with infos about every file copied
     */
    Future<List<JsonObject>> copyDocumentToWorkspace(UserNextcloud.TokenProvider userSession, UserInfos user, List<String> filesPath, String parentId);

    /**
     * Move all the files listed in the filesPath from nextcloud to local.
     * @param userSession       User session
     * @param user              User infos
     * @param filesPath         Path of all the files to move
     * @param parentId          Identifier of the previous folder if moving in a folder
     * @return                  Future list of JsonObject with infos about every file moved
     */
    Future<List<JsonObject>> moveDocumentToWorkspace(UserNextcloud.TokenProvider userSession, UserInfos user, List<String> filesPath, String parentId);

    /**
     * Move all the documents listed in id idList from workspace to Nextcloud
     * @param userSession   User session
     * @param user          User infos
     * @param idList        Identifier of all the documents to move
     * @param parentName    Name of the parent folder in nextcloud
     * @return              Future Json with all the status infos about the move.
     */
    Future<JsonObject> moveDocumentsFromWorkspaceToNC(UserNextcloud.TokenProvider userSession, UserInfos user, List<String> idList, String parentName);

    /**
     * Copy all the documents listed in id idList from workspace to Nextcloud
     * @param userSession   User session
     * @param user          User infos
     * @param idList        Identifier of all the documents to move
     * @param parentName    Name of the parent folder in nextcloud
     * @return              Future Json with all the status infos about the copy.
     */
    Future<JsonObject> copyDocumentsFromWorkspaceToNC(UserNextcloud.TokenProvider userSession, UserInfos user, List<String> idList, String parentName);

    /**
     * Create a new folder in the Nextcloud space
     * @param userSession   User session
     * @param path          Path of the new folder in Nextcloud
     * @return              Future JsonObject with the status of the creation
     */
    Future<JsonObject> createFolderNextcloud(UserNextcloud.TokenProvider userSession, String path);

}
