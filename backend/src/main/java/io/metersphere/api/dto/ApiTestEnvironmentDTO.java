package io.metersphere.api.dto;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class ApiTestEnvironmentDTO extends ApiTestEnvironmentWithBLOBs {
    private List<String> uploadIds;
}
