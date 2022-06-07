package fr.openent.nextcloud.core.enums;

public enum WorkspaceEventBusActions {
    ADDFOLDER("addFolder"),
    DELETE("delete");

    private final String action;

    WorkspaceEventBusActions(String action) {
        this.action = action;
    }

    public String action() {
        return this.action;
    }
}
