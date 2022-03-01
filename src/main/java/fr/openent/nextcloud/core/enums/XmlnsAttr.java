package fr.openent.nextcloud.core.enums;

public enum XmlnsAttr {
    D("DAV:"),
    OC("http://owncloud.org/ns"),
    NC("http://nextcloud.org/ns");


    private final String attribute;

    XmlnsAttr(String attribute) {
        this.attribute = attribute;
    }

    public String attr() {
        return this.attribute;
    }
}
