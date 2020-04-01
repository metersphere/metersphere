package io.metersphere.base.domain;

import java.io.Serializable;

public class LoadTestReportWithBLOBs extends LoadTestReport implements Serializable {
    private String description;

    private String content;

    private static final long serialVersionUID = 1L;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }
}