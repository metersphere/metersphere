package io.metersphere.api.dto;

import lombok.Data;

@Data
public class ApiExecResultDTO {
    // 接口定义、接口用例、场景等的id
    private String resourceId;

    private String execResult;
}
