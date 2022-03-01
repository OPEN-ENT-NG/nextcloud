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
        private final Number used;
        private byte maxQuota;

        public Quota(JsonObject quota) {
            this.used = quota.getNumber(Field.USED, 0);
            try {
                this.maxQuota = Integer.valueOf(quota.getString(Field.QUOTA, "-1")).byteValue();
            } catch (NumberFormatException e) {
                this.maxQuota = 0;
            }
        }

        public JsonObject toJSON() {
            return new JsonObject()
                    .put(Field.USED, this.used)
                    .put(Field.QUOTA, this.maxQuota > 0 ? QuotaHelper.humanReadableByteCount(this.maxQuota) : "none");
        }
    }

    public static class RequestBody {
        private String userId;
        private String password;

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
                    .put(Field.PASSWORD, this.password);
        }
    }
}
