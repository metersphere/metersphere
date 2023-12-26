package io.metersphere.dto;

import io.metersphere.api.domain.ApiTestCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiTestCaseProviderDTO extends ApiTestCase {

    @Schema(description = "版本名称")
    private String versionName;

}
