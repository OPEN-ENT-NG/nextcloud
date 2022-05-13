package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.core.constants.Field;
import fr.wseduc.swift.utils.FileUtils;
import fr.wseduc.webutils.DefaultAsyncResult;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.bus.WorkspaceHelper;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserInfos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static fr.wseduc.webutils.Utils.isNotEmpty;

public class FileHelper {
    private static final Logger log = LoggerFactory.getLogger(FileHelper.class);

    private FileHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method will fetch all uploaded files from your {@link HttpServerRequest} request and upload them into your
     * storage and return each of them an object {@link Attachment}
     * <p>
     * <b>WARNING</b><br/>
     * Must SPECIFY a custom header where you can define a number to decide whether or not your upload should finish
     * and complete (e.g adding "Files" as custom header as key and its value the number of file to loop/fetch will
     * allow your uploadHandler to trigger n callback with different upload object)
     * </p>
     *
     * @param headerCount the name of your header used to fetch your total number file
     * @param request     request HttpServerRequest
     * @param storage     Storage vertx
     * @param vertx       Vertx vertx
     * @return list of {@link Attachment} (and all your files will be uploaded)
     * (process will continue in background to stream all these files in your storage)
     */
    public static Future<List<Attachment>> uploadMultipleFiles(String headerCount, HttpServerRequest request, Storage storage,
                                                               Vertx vertx) {
        request.response().setChunked(true);
        request.setExpectMultipart(true);
        Promise<List<Attachment>> promise = Promise.promise();
        String totalFilesToUpload = request.getHeader(headerCount);
        AtomicBoolean responseSent = new AtomicBoolean();
        responseSent.set(false);

        List<String> fileIds = new ArrayList<>();
        AtomicReference<List<String>> pathIds = new AtomicReference<>();

        for (int i = 0; i < Integer.parseInt(totalFilesToUpload); i++) {
            fileIds.add(UUID.randomUUID().toString());
        }

        // return empty arrayList if no header is sent (meaning no files to upload)
        if (totalFilesToUpload.isEmpty() || Integer.parseInt(totalFilesToUpload) == 0) {
            promise.complete(new ArrayList<>());
            return promise.future();
        }
        AtomicInteger incrementFile = new AtomicInteger(0);
        List<Attachment> listMetadata = new ArrayList<>();
        request.exceptionHandler(event -> {
            String messageToFormat = "[Nextcloud@%s::uploadMultipleFiles] An error has occurred during http request process: %s";
            PromiseHelper.reject(log, messageToFormat, FileHelper.class.getSimpleName(), event.getCause(), promise);
        });
        request.pause();

        request.uploadHandler(upload -> {
            String finalPath = pathIds.get().get(incrementFile.get());
            final JsonObject metadata = FileUtils.metadata(upload);
            listMetadata.add(new Attachment(fileIds.get(incrementFile.get()), new Metadata(metadata)));

            upload.streamToFileSystem(finalPath);
            incrementFile.set(incrementFile.get() + 1);


            upload.exceptionHandler(err -> {
                String messageToFormat = "[Nextcloud@%s::uploadMultipleFiles] An exception has occurred during http upload process: %s";
                PromiseHelper.reject(log, messageToFormat, FileHelper.class.getSimpleName(), err, promise);
            });
            upload.endHandler(aVoid -> {
                if (incrementFile.get() == Integer.parseInt(totalFilesToUpload) && !responseSent.get()) {
                    responseSent.set(true);
                    promise.complete(listMetadata);
                }
            });

        });

        List<Future<String>> makeFolders = new ArrayList<>();

        for (int i = 0; i < fileIds.size(); i++) {
            makeFolders.add(makeFolder(storage, vertx, fileIds, i));
        }

        FutureHelper.all(makeFolders).onSuccess(success -> {
            pathIds.set(success.list());
            request.resume();
        }).onFailure(failure -> {
            String messageToFormat = "[Nextcloud@%s::uploadMultipleFiles] An exception has occurred during creating folders: %s";
            PromiseHelper.reject(log, messageToFormat, FileHelper.class.getSimpleName(), failure, promise);
        });

        return promise.future();
    }

    private static Future<String> makeFolder(Storage storage, Vertx vertx, List<String> fileIds, int i) {
        Promise<String> promise = Promise.promise();
        String path = " ";
        try {
            path = getFilePath(fileIds.get(i), storage.getBucket());
        } catch (FileNotFoundException e) {
            String messageToFormat = "[Nextcloud@%s::makeFolder] error while creation path : %s";
            PromiseHelper.reject(log, messageToFormat, FileHelper.class.getSimpleName(), e, promise);
        }
        String finalPath = path;
        mkdirsIfNotExists(vertx.fileSystem(), path, event -> {
            if (event.succeeded()) {
                promise.complete(finalPath);
            } else {
                String messageToFormat = "[Nextcloud@%s::makeFolder] error while creation folder : %s";
                PromiseHelper.reject(log, messageToFormat, FileHelper.class.getSimpleName(), event.cause(), promise);
            }
        });
        return promise.future();
    }

    private static void mkdirsIfNotExists(FileSystem fileSystem, String path, final Handler<AsyncResult<Void>> handler) {
        final String dir = org.entcore.common.utils.FileUtils.getParentPath(path);
        fileSystem.exists(dir, event -> {
            if (event.succeeded()) {
                if (Boolean.FALSE.equals(event.result())) {
                    fileSystem.mkdirs(dir, handler);
                } else {
                    handler.handle(new DefaultAsyncResult<>((Void) null));
                }
            } else {
                handler.handle(new DefaultAsyncResult<>(event.cause()));
            }
        });
    }

    private static String getFilePath(String file, final String bucket) throws FileNotFoundException {
        if (isNotEmpty(file)) {
            final int startIdx = file.lastIndexOf(File.separatorChar) + 1;
            final int extIdx = file.lastIndexOf('.');
            String filename = (extIdx > 0) ? file.substring(startIdx, extIdx) : file.substring(startIdx);
            if (isNotEmpty(filename)) {
                final int l = filename.length();
                if (l < 4) {
                    filename = "0000".substring(0, 4 - l) + filename;
                }
                return bucket + filename.substring(l - 2) + File.separator + filename.substring(l - 4, l - 2) +
                        File.separator + filename;
            }

        }
        throw new FileNotFoundException(String.format("[Nextcloud@::getFilePath]Invalid file : %s", file));
    }

    public static Future<JsonObject> writeBuffer(Storage storage, Buffer buff, final String contentType, final String filename) {
        Promise<JsonObject> promise = Promise.promise();
        buff = buff == null ? Buffer.buffer(" ") : buff;
        storage.writeBuffer(buff, contentType, filename, res -> {
            if (!Field.ERROR.equals(res.getString(Field.STATUS))) {
                promise.complete(res);
            } else {
                String messageToFormat = "[Nextcloud@%s::writeBuffer] Error while storing file to workspace : %s";
                PromiseHelper.reject(log, messageToFormat, FileHelper.class.getSimpleName(), new Exception(res.getString(Field.ERROR)), promise);
            }
        });

        return promise.future();
    }

    /**
     * Handler which adds document into the MongoDB after downloading it from the NC server
     * @param uploaded      Data about the download (metadata, title ...)
     * @param user          User infos
     * @param fileName      Name of the file on the NC server
     * @return              The handler
     */
    public static Future<JsonObject> addFileReference(JsonObject uploaded, UserInfos user, String fileName,
                                                      WorkspaceHelper workspaceHelper) {
        Promise<JsonObject> promise = Promise.promise();
        workspaceHelper.addDocument(uploaded, user, fileName, Field.APP, false, null,
                resDoc -> {
                    if (resDoc.succeeded()) {
                        promise.complete(resDoc.result().body());
                    } else {
                        String messageToFormat = "[Nextcloud@%s::addDocument] Error while adding document : %s";
                        PromiseHelper.reject(log, messageToFormat, FileHelper.class.getSimpleName(), resDoc, promise);
                    }});

        return promise.future();
    }
}
