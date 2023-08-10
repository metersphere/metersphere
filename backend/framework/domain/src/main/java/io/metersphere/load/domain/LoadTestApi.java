package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestApi implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_api.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_api.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "接口场景或用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_api.api_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_api.api_id.length_range}", groups = {Created.class, Updated.class})
    private String apiId;

    @Schema(description =  "性能测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_api.load_test_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_api.load_test_id.length_range}", groups = {Created.class, Updated.class})
    private String loadTestId;

    @Schema(description =  "环境ID")
    private String envId;

    @Schema(description =  "类型: SCENARIO, CASE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_api.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{load_test_api.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description =  "关联版本")
    private Integer apiVersion;

    private static final long serialVersionUID = 1L;
}