package fr.openent.nextcloud.config;

import fr.openent.nextcloud.core.constants.Field;
import io.vertx.core.json.JsonObject;

public class NextcloudConfig {
    private final String host;
    private final String username;
    private final String password;
    private final String ocsEndpoint;
    private final String webdavEndpoint;
    private final String quota;
    private final Boolean isNextcloudUrlHidden;

    public NextcloudConfig(JsonObject config) {
        this.host = config.getString(Field.NEXTCLOUDHOST, null);
        this.username = config.getJsonObject(Field.ADMINCREDENTIAL, new JsonObject()).getString(Field.USERNAME, null);
        this.password = config.getJsonObject(Field.ADMINCREDENTIAL, new JsonObject()).getString(Field.PASSWORD, null);
        this.ocsEndpoint = config.getJsonObject(Field.ENDPOINT, new JsonObject()).getString(Field.OCS_ENDPOINT_API, null);
        this.webdavEndpoint = config.getJsonObject(Field.ENDPOINT, new JsonObject()).getString(Field.WEBDAV_ENDPOINT_API, null);
        this.quota = config.getString(Field.QUOTA, "2 GB");
        this.isNextcloudUrlHidden = config.getBoolean(Field.KEBAB_IS_NEXTCLOUD_URL_HIDDEN, false);
    }

    public String host() {
        return this.host;
    }

    public String username() { return this.username; }

    public String password() { return this.password; }

    public String ocsEndpoint() { return this.ocsEndpoint; }

    public String webdavEndpoint() { return this.webdavEndpoint; }

    public String quota() { return this.quota; }

    public Boolean isNextcloudUrlHidden() { return this.isNextcloudUrlHidden; }
}
