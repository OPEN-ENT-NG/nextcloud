package fr.openent.nextcloud.config;

import fr.openent.nextcloud.core.constants.Field;
import io.vertx.core.json.JsonObject;

public class NextcloudConfig {
    private final String host;
    private final String username;
    private final String password;
    private final String ocsEndpoint;
    private final String webdavEndpoint;

    public NextcloudConfig(JsonObject config) {
        this.host = config.getString(Field.NEXTCLOUDHOST, null);
        this.username = config.getJsonObject(Field.ADMINCRENDENTIAL, new JsonObject()).getString(Field.USERNAME, null);
        this.password = config.getJsonObject(Field.ADMINCRENDENTIAL, new JsonObject()).getString(Field.PASSWORD, null);
        this.ocsEndpoint = config.getJsonObject(Field.ENDPOINT, new JsonObject()).getString(Field.OCS_ENDPOINT_API, null);
        this.webdavEndpoint = config.getJsonObject(Field.ENDPOINT, new JsonObject()).getString(Field.WEBDAV_ENDPOINT_API, null);
    }

    public String host() {
        return this.host;
    }

    public String username() { return this.username; }

    public String password() { return this.password; }

    public String ocsEndpoint() { return this.ocsEndpoint; }

    public String webdavEndpoint() { return this.webdavEndpoint; }
}
