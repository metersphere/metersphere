package io.metersphere.project.dto;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加载选项时，标记字段来自哪里，表字段，自定义字段，报告定义字段
 *
 * @author guoyuqi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageTemplateFieldDTO extends OptionDTO {

    @Schema(description = "字段来源")
    private String fieldSource;
}
