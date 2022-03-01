package fr.openent.nextcloud.model;

import fr.openent.nextcloud.core.enums.XmlnsAttr;
import io.vertx.core.json.JsonObject;

public class XmlnsOptions {

    private String webDavTag;
    private String ownCloudTag;
    private String nextcloudTag;

    public XmlnsOptions setWebDavTag(XmlnsAttr xmlnsAttr) {
        this.webDavTag = xmlnsAttr.attr();
        return this;
    }

    public XmlnsOptions setOwnCloudTag(XmlnsAttr xmlnsAttr) {
        this.ownCloudTag = xmlnsAttr.attr();
        return this;
    }

    public XmlnsOptions setNextcloudTag(XmlnsAttr xmlnsAttr) {
        this.nextcloudTag = xmlnsAttr.attr();
        return this;
    }

    public JsonObject toJSON() {
        JsonObject xmlns = new JsonObject();
        if (this.webDavTag != null) {
            xmlns.put("xmlns:d", this.webDavTag);
        }
        if (this.ownCloudTag != null) {
            xmlns.put("xmlns:oc", this.ownCloudTag);
        }
        if (this.nextcloudTag != null) {
            xmlns.put("xmlns:nc", this.nextcloudTag);
        }
        return xmlns;
    }

}
