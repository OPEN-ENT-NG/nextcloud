package fr.openent.nextcloud.core.enums;

public enum WorkspaceEventBusActions {
    ADDFOLDER("addFolder"),
    DELETE("delete"),
    GETDOCUMENT("getDocument"),
    LIST("list");

    private final String action;

    WorkspaceEventBusActions(String action) {
        this.action = action;
    }

    public String action() {
        return this.action;
    }
}
