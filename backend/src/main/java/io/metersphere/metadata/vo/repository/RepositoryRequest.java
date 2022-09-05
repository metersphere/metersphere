package io.metersphere.metadata.vo.repository;

import lombok.Data;

@Data
public class RepositoryRequest {
    private String fileMetadataId;
    private String filePath;
    private String commitId;
}
