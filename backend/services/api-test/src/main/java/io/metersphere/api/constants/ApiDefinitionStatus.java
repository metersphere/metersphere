package io.metersphere.api.constants;

import lombok.Getter;

/**
 * @author: LAN
 * @date: 2023/11/16 10:42
 * @version: 1.0
 */
@Getter
public enum ApiDefinitionStatus {
    PREPARE("Prepare"),
    UNDERWAY("Underway"),
    DEBUGGING("Debugging"),
    OBSOLETE("Obsolete"),
    COMPLETED("Completed");

    private final String value;

    ApiDefinitionStatus(String value) {
        this.value = value;
    }

}
