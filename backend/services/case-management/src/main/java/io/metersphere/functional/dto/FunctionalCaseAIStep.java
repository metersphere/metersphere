package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseAIStep {

    //步骤id
    @Schema(description = "步骤id")
    private String id;

    //步骤序号
    @Schema(description = "步骤序号")
    private Integer num;

    //步骤描述
    @Schema(description = "步骤描述")
    private String desc;

    //步骤结果
    @Schema(description = "步骤结果")
    private String result;
}
