package io.metersphere.constants;

import org.apache.commons.lang3.StringUtils;

public enum IssueStatus {
    status_new("new", "new"),
    status_created("created", "new"),
    status_resolved("resolved", "resolved"),
    status_closed("closed", "closed"),
    status_active("active", "active"),
    status_delete("delete", "delete"),
    status_in_progress("in_progress", "in_progress"),
    status_rejected("rejected", "rejected"),
    status_upcoming("upcoming", "upcoming"),
    status_reopened("reopened", "reopened");

    private String name;
    private String i18nKey;

    IssueStatus(String name, String i18nKey) {
        this.name = name;
        this.i18nKey = i18nKey;
    }

    public String getName() {
        return name;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public static IssueStatus getEnumByName(String name) {
        IssueStatus[] issueStatus = IssueStatus.values();
        for (int i = 0; i < issueStatus.length; i++) {
            if (StringUtils.equals(issueStatus[i].getName(), name)) {
                return issueStatus[i];
            }
        }
        return null;
    }

}
