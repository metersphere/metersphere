package io.metersphere.bug.enums;

import lombok.Getter;

/**
 * @author song-cc-rock
 */

@Getter
public enum BugPlatform {

    /**
     * 本地
     */
    LOCAL("Local"),
    /**
     * Jira
     */
    JIRA("Jira"),
    /**
     * Tapd
     */
    TAPD("Tapd"),
    /**
     * 禅道
     */
    ZENDAO("Zentao"),
    /**
     * Azure
     */
    AZURE("Azure");

    private final String name;

    BugPlatform(String name) {
        this.name = name;
    }

}
