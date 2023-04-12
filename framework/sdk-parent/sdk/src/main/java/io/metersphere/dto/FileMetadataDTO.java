package io.metersphere.dto;

import io.metersphere.base.domain.FileMetadataWithBLOBs;
import lombok.Data;

@Data
public class FileMetadataDTO extends FileMetadataWithBLOBs {
    private String moduleName;
}
