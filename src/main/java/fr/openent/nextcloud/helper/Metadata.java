package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.core.constants.Field;
import io.vertx.core.json.JsonObject;

public class Metadata   {
    private final String name;
    private final String contentType;
    private final String contentTransferEncoding;
    private final String filename;
    private final Integer size;
    private final String charset;

    public Metadata(JsonObject metadata) {
        this.name = metadata.getString(Field.NAME);
        this.contentType = metadata.getString(Field.CONTENT_TYPE);
        this.contentTransferEncoding = metadata.getString(Field.CONTENTTRANSFERTENCODING);
        this.filename = metadata.getString(Field.FILENAMELOWER);
        this.size = metadata.getInteger(Field.SIZE);
        this.charset = metadata.getString(Field.CHARSET);
    }

    public JsonObject toJSON() {
        return new JsonObject()
                .put(Field.NAME, this.name)
                .put(Field.CONTENT_TYPE, this.contentType)
                .put(Field.CONTENTTRANSFERTENCODING, this.contentTransferEncoding)
                .put(Field.FILENAMELOWER, this.filename)
                .put(Field.SIZE, this.size)
                .put(Field.CHARSET, this.charset);
    }

    public String filename() {
        return filename;
    }
}
