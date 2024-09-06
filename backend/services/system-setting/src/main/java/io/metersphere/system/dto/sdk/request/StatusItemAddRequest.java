package io.metersphere.system.dto.sdk.request;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class StatusItemAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    private String scopeId;

    @Schema(description = "状态名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_item.name.not_blank}")
    @Size(min = 1, max = 255, message = "{status_item.name.length_range}")
    private String name;

    @Schema(description = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{status_item.scene.not_blank}")
    @EnumValue(enumClass = TemplateScene.class)
    private String scene;

    @Schema(description = "状态说明")
    @Size(max = 1000)
    private String remark;

    @Schema(description = "所有状态都可以流转到该状态")
    private Boolean allTransferTo;
}