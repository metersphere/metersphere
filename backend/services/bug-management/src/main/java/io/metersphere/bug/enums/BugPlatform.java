package io.metersphere.bug.enums;

/**
 * @author song-cc-rock
 */

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

    public String getName() {
        return name;
    }
}
