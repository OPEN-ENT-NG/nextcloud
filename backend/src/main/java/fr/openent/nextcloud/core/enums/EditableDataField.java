package fr.openent.nextcloud.core.enums;

public enum EditableDataField {
    EMAIL("email"),
    PASSWORD("password"),
    QUOTA("quota");


    private final String dataField;

    EditableDataField(String dataField) {
        this.dataField = dataField;
    }

    public String dataField() {
        return this.dataField;
    }
}
