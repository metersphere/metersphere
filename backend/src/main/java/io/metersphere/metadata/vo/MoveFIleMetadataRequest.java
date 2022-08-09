package io.metersphere.metadata.vo;

import lombok.Data;

import java.util.List;

@Data
public class MoveFIleMetadataRequest {
    private List<String> metadataIds;
    private String moduleId;
}
