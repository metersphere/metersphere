package io.metersphere.project.dto;

import io.metersphere.sdk.constants.FakeErrorType;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FakeErrorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.project_id.not_blank}")
    private String projectId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.name.not_blank}")
    private String name;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.type.not_blank}")
    private String type;

    @Schema(description = "匹配规则-内容类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @EnumValue(enumClass = FakeErrorType.class, groups = {Created.class})
    @NotBlank(message = "{fake_error.resp_type.not_blank}")
    private String respType;

    @Schema(description = "匹配规则-操作类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.relation.not_blank}")
    private String relation;

    @Schema(description = "匹配规则-表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.expression.not_blank}")
    private String expression;

    @Schema(description = "状态")
    private Boolean enable;


}
