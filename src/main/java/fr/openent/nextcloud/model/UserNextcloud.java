package fr.openent.nextcloud.model;

import fr.openent.nextcloud.core.constants.Field;
import fr.openent.nextcloud.helper.QuotaHelper;
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
        private String password;

        public String userId() {
            return userId;
        }

        public String password() {
            return password;
        }

        public RequestBody setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public RequestBody setPassword(String password) {
            this.password = password;
            return this;
        }

        public JsonObject toJSON() {
            return new JsonObject()
                    .put(Field.USERID, this.userId)
                    .put(Field.PASSWORD, this.password)
                    .put(Field.QUOTA, "2 GB");
        }
    }

    public static class TokenProvider {
        private String userId;
        private String loginName;
        private String token;

        public String userId() {
            return userId;
        }

        public String loginName() {
            return loginName;
        }

        public String token() {
            return token;
        }

        public TokenProvider setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public TokenProvider setLoginName(String loginName) {
            this.loginName = loginName;
            return this;
        }

        public TokenProvider setToken(String token) {
            this.token = token;
            return this;
        }

        public JsonObject toJSON() {
            return new JsonObject()
                    .put(Field.LOGINNAME, this.loginName)
                    .put(Field.TOKEN, this.token);
        }

        public boolean isEmpty() {
            return this.userId == null
                    && this.loginName == null
                    && this.token == null;
        }
    }
}
