package io.metersphere.commons.enums;

public enum ApiTestDataStatus {
    UNDERWAY("Underway"),
    TRASH("Trash"),
    PREPARE("Prepare"),
    COMPLETED("Completed");

    private String value;

    ApiTestDataStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
