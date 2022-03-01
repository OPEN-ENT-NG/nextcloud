package fr.openent.nextcloud.model;

import fr.openent.nextcloud.core.constants.Field;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.nio.file.Paths;

public class Document {

    private final String path;
    private final String owner;
    private final String contentType;
    private final Number size;
    private final Number favorite;
    private final String etag;
    private final Number fileId;

    public Document(JsonObject documentResponse) {
        this.path = documentResponse.getString(Field.D_HREF);
        JsonObject props;
        try {
            // if PROPSTAT is a JsonArray (likely to be a folder format one)
            if (documentResponse.getValue(Field.D_PROPSTAT) instanceof JsonArray) {
                props = documentResponse.getJsonArray(Field.D_PROPSTAT, new JsonArray())
                        .getJsonObject(0).getJsonObject(Field.D_PROP);
            }
            // if PROPSTAT is a JsonObject (likely to be a file format one)
            else if (documentResponse.getValue(Field.D_PROPSTAT) instanceof JsonObject) {
                props = documentResponse.getJsonObject(Field.D_PROPSTAT, new JsonObject())
                        .getJsonObject(Field.D_PROP, new JsonObject());
            } else {
                props = new JsonObject();
            }
        } catch (ClassCastException | NullPointerException | IndexOutOfBoundsException e) {
            String message = String.format("[Nextcloud@%s] An error has occurred during modeling Documents : %s, " +
                            "adding empty Json instead",
                    this.getClass().getSimpleName(), e.getMessage());
            Logger log = LoggerFactory.getLogger(Document.class);
            log.error(message);
            props = new JsonObject();
        }
        this.owner = props.getString(Field.OC_OWNER_DISPLAY_NAME);
        this.contentType = props.getString(Field.D_GETCONTENTTYPE);
        this.size = props.getInteger(Field.OC_SIZE);
        this.favorite = props.getInteger(Field.OC_FAVORITE);
        this.etag = props.getString(Field.D_GETETAG);
        this.fileId = props.getInteger(Field.OC_FILEID);
    }

    public JsonObject toJSON() {
        return new JsonObject()
                .put(Field.PATH, this.path)
                .put(Field.DISPLAYNAME, Paths.get(this.path).getFileName().toString())
                .put(Field.OWNERDISPLAYNAME, this.owner)
                .put(Field.CONTENTTYPE, this.contentType)
                .put(Field.SIZE, this.size)
                .put(Field.FAVORITE, this.favorite)
                .put(Field.ETAG, this.etag)
                .put(Field.FILEID, this.fileId)
                .put(Field.ISFOLDER, this.contentType == null);
    }


    public static class RequestBody {

        public JsonObject toJSON() {
            return new JsonObject().put(Field.D_PROPFIND,
                    new JsonObject().put(Field.D_PROP, new JsonObject()
                            .put(Field.D_GETLASTMODIFIED, new JsonObject())
                            .put(Field.D_GETETAG, new JsonObject())
                            .put(Field.D_GETCONTENTTYPE, new JsonObject())
                            .put(Field.D_RESOURCETYPE, new JsonObject())
                            .put(Field.OC_FILEID, new JsonObject())
                            .put(Field.OC_PERMISSIONS, new JsonObject())
                            .put(Field.OC_SIZE, new JsonObject())
                            .put(Field.D_GETCONTENTLENGTH, new JsonObject())
                            .put(Field.NC_HAS_PREVIEW, new JsonObject())
                            .put(Field.OC_FAVORITE, new JsonObject())
                            .put(Field.OC_COMMENTS_UNREAD, new JsonObject())
                            .put(Field.OC_OWNER_DISPLAY_NAME, new JsonObject())
                            .put(Field.OC_SHARE_TYPE, new JsonObject())));
        }
    }

}
