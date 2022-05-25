package fr.openent.nextcloud.core.enums;

public enum EventBusActions {
    ADDFOLDER("addFolder"),
    DELETE("delete");

    private final String action;

    EventBusActions(String action) {
        this.action = action;
    }

    public String action() {
        return this.action;
    }
}
