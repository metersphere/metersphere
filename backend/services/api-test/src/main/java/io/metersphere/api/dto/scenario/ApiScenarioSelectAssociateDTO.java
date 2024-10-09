package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.system.dto.ModuleSelectDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guoyuqi
 */

@Data
public class ApiScenarioSelectAssociateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String scenarioId;

    @Schema(description = "是否选择所有模块")
    private boolean selectAllModule = false;

    @Schema(description = "模块下的id集合属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, ModuleSelectDTO> moduleMaps;

    @Schema(description = "关联类型 COPY:复制  REF:引用")
    @EnumValue(enumClass = ApiScenarioStepRefType.class)
    @NotBlank
    private String refType;

    @Schema(description = "关联关系的type(接口定义：API/接口用例：API_CASE/场景：API_SCENARIO)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String associateType;

    @Schema(description = "项目id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "协议")
    private List<String> protocols;

}
