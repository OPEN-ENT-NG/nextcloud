package fr.openent.nextcloud.helper;

import fr.openent.nextcloud.model.Document;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;


public class DocumentHelper {

    private DocumentHelper() { throw new IllegalStateException("Helper DocumentHelper class"); }

    /**
     * Returns Array of {@link Document} with a list of json passed
     *
     * @param   documents   {@link JsonArray}
     * @return  A formatted JsonObject
     */
    public static List<Document> documents(JsonArray documents) {
        return documents.stream().map(d -> new Document((JsonObject) d)).collect(Collectors.toList());
    }

    /**
     * Returns Array of {@link JsonObject} also usable as {@link JsonArray} with a list of documents passed
     *
     * @param   documents  List of {@link Document}
     * @return  A formatted JsonObject list
     */
    public static List<JsonObject> toListJsonObject(List<Document> documents) {
        return documents.stream().map(Document::toJSON).collect(Collectors.toList());
    }
}