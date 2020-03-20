package io.metersphere.controller.request.testplan;

import io.metersphere.base.domain.FileMetadata;

import java.util.List;

public class EditTestPlanRequest extends TestPlanRequest {
    private List<FileMetadata> updatedFileList;

    public List<FileMetadata> getUpdatedFileList() {
        return updatedFileList;
    }

    public void setUpdatedFileList(List<FileMetadata> updatedFileList) {
        this.updatedFileList = updatedFileList;
    }
}
