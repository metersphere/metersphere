package io.metersphere.api.dto.scenario;

import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ApiScenarioStepFileCopyRequest {
    /**
     * 记录是从哪个步骤复制来的
     * 如果没有传步骤详情
     * 保存时需要根据这个字段查询原步骤详情保存
     */
    @Schema(description = "复制的目标步骤ID")
    private String copyFromStepId;

    @Schema(description = "资源id")
    private String resourceId;

    /**
     * {@link ApiScenarioStepType}
     */
    @Schema(description = "步骤类型/API/CASE等")
    @EnumValue(enumClass = ApiScenarioStepType.class)
    private String stepType;

    @Schema(description = "是否是临时文件")
    @NotNull
    private Boolean isTempFile;

    @Schema(description = "文件数组")
    @NotEmpty
    private List<String> fileIds;
}
