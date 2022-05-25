package fr.openent.nextcloud.model;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.service.impl.DefaultDocumentsService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is the representation of a folder in the nextcloud server.
 */
public class NextcloudFolder {
    private String name;
    private List<JsonObject> folderItemsInfos = new ArrayList<>();
    private String workspaceId;
    private String path;
    public NextcloudFolder(JsonArray folderInfos) {
        try {
            this.folderItemsInfos = folderInfos.stream().map(fileInfo -> new JsonObject(
                    fileInfo.toString())).collect(Collectors.toList());
            this.name = this.folderItemsInfos.get(0).getString(Field.DISPLAYNAME);
            //removing first one because it is the folder itself.
            this.folderItemsInfos.remove(0);
        } catch (Exception e) {
            log.error("[Nextcloud@::NextcloudFolder] Error while instantiating NextcloudFolder class");
        }
    }
    private final Logger log = LoggerFactory.getLogger(DefaultDocumentsService.class);

    public List<JsonObject> getFolderItemsInfos() {
        return folderItemsInfos;
    }

    public String getName() {
        return name;
    }

    public List<String> getFolderItemsNames() {
        return this.folderItemsInfos.stream().map(fileInfos -> new JsonObject(fileInfos.toString()).getString(Field.DISPLAYNAME)).collect(Collectors.toList());
    }

    public List<String> getFolderItemPath() {
        return this.getFolderItemsNames().stream().map(r -> (path != null ? path + "/" : "") + r).collect(Collectors.toList());
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
