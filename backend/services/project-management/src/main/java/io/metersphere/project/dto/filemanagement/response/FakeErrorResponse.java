package io.metersphere.project.dto.filemanagement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class FakeErrorResponse implements Serializable {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "匹配规则-内容类型/header/data/body", requiredMode = Schema.RequiredMode.REQUIRED)
    private String respType;

    @Schema(description = "匹配规则-操作类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String relation;

    @Schema(description = "匹配规则-表达式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expression;

    @Schema(description = "状态")
    private Boolean enable;

}
