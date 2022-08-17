package io.metersphere.metadata.vo;

import lombok.Data;

@Data
public class DumpFileRequest {
    private String resourceId;
    private String moduleId;
    private String fileName;
    private String projectId;
    private boolean isCsv;
}
