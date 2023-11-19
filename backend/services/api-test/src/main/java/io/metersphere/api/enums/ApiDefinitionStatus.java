package io.metersphere.api.enums;

/**
 * @author: LAN
 * @date: 2023/11/16 10:42
 * @version: 1.0
 */
public enum ApiDefinitionStatus {
    PREPARE("Prepare"),
    UNDERWAY("Underway"),
    DEBUGGING("Debugging"),
    OBSOLETE("Obsolete"),
    COMPLETED("Completed");

    private String value;

    ApiDefinitionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
