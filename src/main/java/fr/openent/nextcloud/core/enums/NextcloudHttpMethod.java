package fr.openent.nextcloud.core.enums;

public enum NextcloudHttpMethod {
    PROPFIND("PROPFIND");


    private final String httpMethod;

    NextcloudHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String method() {
        return this.httpMethod;
    }
}
