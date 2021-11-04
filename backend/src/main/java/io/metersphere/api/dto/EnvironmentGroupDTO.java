package io.metersphere.api.dto;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class EnvironmentGroupDTO extends ApiTestEnvironmentWithBLOBs {
    private String envGroupId;
    private String envId;
    private String description;
}
