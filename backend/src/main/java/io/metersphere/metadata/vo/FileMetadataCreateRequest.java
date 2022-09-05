package io.metersphere.metadata.vo;

import io.metersphere.base.domain.FileMetadataWithBLOBs;
import lombok.Data;

@Data
public class FileMetadataCreateRequest extends FileMetadataWithBLOBs {
    private String repositoryBranch;
    private String repositoryPath;
}
