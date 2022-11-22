package io.metersphere.api.dto;

import io.metersphere.request.BodyFile;
import lombok.Data;

import java.util.List;

@Data
public class BodyFileRequest {
    private String reportId;
    private List<BodyFile> bodyFiles;
}
