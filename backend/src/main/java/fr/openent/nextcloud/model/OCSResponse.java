package fr.openent.nextcloud.model;

import fr.openent.nextcloud.core.constants.Field;
import io.vertx.core.json.JsonObject;

// Open Collaboration Services model
public class OCSResponse {

    private final Meta meta;
    private final JsonObject data;

    public OCSResponse(JsonObject ocsResponse) {
        this.meta = new Meta(ocsResponse.getJsonObject(Field.META, new JsonObject()));
        this.data = ocsResponse.getValue(Field.DATA) instanceof JsonObject ? ocsResponse.getJsonObject(Field.DATA, new JsonObject()) : new JsonObject();
    }

    public JsonObject toJSON() {
        return new JsonObject()
                .put(Field.META, this.meta.toJSON())
                .put(Field.DATA, this.data);
    }


    public JsonObject data() {
        return this.data;
    }

    public static class Meta {
        private final String status;
        private final Number statusCode;
        private final String message;
        private final String totalItems;
        private final String itemsPerPage;

        public Meta(JsonObject meta) {
            this.status = meta.getString(Field.STATUS);
            this.statusCode = meta.getNumber(Field.STATUSCODE);
            this.message = meta.getString(Field.MESSAGE);
            this.totalItems = meta.getString(Field.TOTALITEMS);
            this.itemsPerPage = meta.getString(Field.ITEMSPERPAGE);
        }

        public JsonObject toJSON() {
            return new JsonObject()
                    .put(Field.STATUS, this.status)
                    .put(Field.STATUSCODE, this.statusCode)
                    .put(Field.MESSAGE, this.message)
                    .put(Field.TOTALITEMS, this.totalItems)
                    .put(Field.ITEMSPERPAGE, this.itemsPerPage);
        }
    }

}
