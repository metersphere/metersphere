package io.metersphere.base.domain;

import java.io.Serializable;

public class FileContent implements Serializable {
    private String fileId;

    private byte[] file;

    private static final long serialVersionUID = 1L;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId == null ? null : fileId.trim();
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}