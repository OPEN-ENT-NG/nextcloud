package fr.openent.nextcloud.core.enums;

public enum NextcloudHttpMethod {
    PROPFIND("PROPFIND"),
    MKCOL("MKCOL"),
    MOVE("MOVE");


    private final String httpMethod;

    NextcloudHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String method() {
        return this.httpMethod;
    }
}
