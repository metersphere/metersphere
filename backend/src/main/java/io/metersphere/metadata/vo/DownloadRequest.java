package io.metersphere.metadata.vo;

import io.metersphere.base.domain.FileMetadata;
import lombok.Data;

import java.util.List;

@Data
public class DownloadRequest {
    private String projectId;
    private List<FileMetadata> requests;
}
