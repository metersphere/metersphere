package io.metersphere.system.controller.dto;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FunctionalCaseDTO extends FunctionalCase {

    @Schema(description =  "评论@的人, 多个以';'隔开")
    private String relatedUsers;

    @Schema(description =  "自定义字段的值")
    private List<OptionDTO> fields;

}
