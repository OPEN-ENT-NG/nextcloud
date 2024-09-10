package fr.openent.nextcloud.model;

import io.vertx.core.json.JsonObject;

import java.util.List;

public class DesktopConfig {
    private int downloadLimit;
    private int uploadLimit;
    private String syncFolder;
    private List<String> excludedExtensions;

    public DesktopConfig(JsonObject configResponse) {
        this.downloadLimit = configResponse.getInteger("downloadLimit");
        this.uploadLimit = configResponse.getInteger("uploadLimit");
        this.syncFolder = configResponse.getString("syncFolder");
        this.excludedExtensions = configResponse.getJsonArray("excludedExtensions").getList();
    }
}
