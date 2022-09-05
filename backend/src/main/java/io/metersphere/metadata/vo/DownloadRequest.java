package io.metersphere.metadata.vo;

import io.metersphere.base.domain.FileMetadataWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class DownloadRequest {
    private String projectId;
    private List<FileMetadataWithBLOBs> requests;
}
