package fr.openent.nextcloud.model;

import fr.openent.nextcloud.config.NextcloudConfig;
import fr.openent.nextcloud.core.constants.Field;
import io.vertx.core.json.JsonObject;

public class UserNextcloud {

    private final String id;
    private final String displayname;
    private final String email;
    private final String phone;
    private final String address;
    private final Quota quota;

    public UserNextcloud(JsonObject user) {
        this.id = user.getString(Field.ID);
        this.displayname = user.getString(Field.DISPLAYNAME);
        this.email = user.getString(Field.EMAIL);
        this.phone = user.getString(Field.PHONE);
        this.address = user.getString(Field.ADDRESS);
        this.quota = new Quota(user.getJsonObject(Field.QUOTA, new JsonObject()));
    }

    public String id() {
        return id;
    }

    public JsonObject toJSON() {
        return new JsonObject()
                .put(Field.ID, this.id)
                .put(Field.DISPLAYNAME, this.displayname)
                .put(Field.EMAIL, this.email)
                .put(Field.PHONE, this.phone)
                .put(Field.ADDRESS, this.address)
                .put(Field.QUOTA, this.quota.toJSON());
    }

    public static class Quota {
        private final Long free;
        private final Number used;
        private final Long total;
        private final Double relative;
        private final Number quota;

        public Quota(JsonObject quota) {
            this.free = quota.getLong(Field.FREE, 0L);
            this.used = quota.getInteger(Field.USED, 0);
            this.total = quota.getLong(Field.TOTAL, 0L);
            this.relative = quota.getDouble(Field.RELATIVE, 0.0);
            this.quota = quota.getLong(Field.QUOTA, 0L);
        }

        public JsonObject toJSON() {
            return new JsonObject()
                    .put(Field.FREE, this.free)
                    .put(Field.USED, this.used.longValue())
                    .put(Field.TOTAL, this.total)
                    .put(Field.RELATIVE, this.relative)
                    .put(Field.QUOTA, this.quota);
        }
    }

    public static class RequestBody {
        private String userId;
        private String displayName;
        private String password;

        public String userId() {
            return userId;
        }
        public String displayName() {
            return displayName;
        }

        public String password() {
            return password;
        }

        public RequestBody setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public RequestBody setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public RequestBody setPassword(String password) {
            this.password = password;
            return this;
        }

        public JsonObject toJSON() {
           return toJSON(new NextcloudConfig(new JsonObject()));
        }

        public JsonObject toJSON(NextcloudConfig nextcloudConfig) {
            JsonObject bodyJson = new JsonObject()
                    .put(Field.USERID, this.userId)
                    .put(Field.DISPLAYNAMECAMEL, this.displayName)
                    .put(Field.PASSWORD, this.password);
            if (nextcloudConfig.quota() == null || nextcloudConfig.quota().isEmpty()) {
                bodyJson.put(Field.QUOTA, "2 GB");
            } else {
                bodyJson.put(Field.QUOTA, nextcloudConfig.quota());
            }
            return bodyJson;
        }
    }

    public static class TokenProvider {
        private String userId;
        private String userName;
        private String token;

        public String userId() {
            return userId;
        }

        public String userName() {
            return userName;
        }

        public String token() {
            return token;
        }

        public TokenProvider setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public TokenProvider setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public TokenProvider setToken(String token) {
            this.token = token;
            return this;
        }

        public JsonObject toJSON() {
            return new JsonObject()
                    .put(Field.LOGINNAME, this.userName)
                    .put(Field.TOKEN, this.token);
        }

        public boolean isEmpty() {
            return this.userId == null
                    && this.userName == null
                    && this.token == null;
        }
    }
}
