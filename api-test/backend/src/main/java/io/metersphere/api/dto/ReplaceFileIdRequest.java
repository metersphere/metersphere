package io.metersphere.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReplaceFileIdRequest {
    private String newFileMetadataId;
    private String oldFileMetadataId;
    private List<String> apiIdList = new ArrayList<>();
    private List<String> apiTestCaseIdList = new ArrayList<>();
    private List<String> apiScenarioIdList = new ArrayList<>();
}
