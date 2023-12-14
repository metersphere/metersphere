package io.metersphere.sdk.dto;

import lombok.Data;

@Data
public class RepositoryQuery {
    private String fileMetadataId;
    private String filePath;
    private String commitId;
    private String projectId;
    private long updateTime;
}
