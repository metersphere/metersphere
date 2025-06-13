package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseAiDTO {

    //用例名称
    @Schema(description = "用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    //用例描述
    @Schema(description = "用例描述")
    private String description;

    //前置条件
    @Schema(description =  "前置条件")
    private String prerequisite;

    //步骤描述
    @Schema(description = "步骤描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String steps;

    //步骤描述
    @Schema(description = "步骤描述")
    private String textDescription;

    //预期结果
    @Schema(description = "预期结果")
    private String expectedResult;

    @Schema(description = "编辑模式：步骤模式：STEP/文本模式：TEXT")
    private String caseEditType;
}
