package io.metersphere.base.domain;

import java.io.Serializable;

public class ApiTestFile implements Serializable {
    private String testId;

    private String fileId;

    private static final long serialVersionUID = 1L;

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId == null ? null : testId.trim();
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }
}