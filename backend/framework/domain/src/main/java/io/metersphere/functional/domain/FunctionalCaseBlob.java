package io.metersphere.functional.domain;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例")
@Table("functional_case_blob")
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCaseBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "用例步骤（JSON)，step_model 为 0 时启用", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String steps;


    @Schema(title = "步骤描述，step_model 为 1 时启用", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String stepDescription;


    @Schema(title = "预期结果，step_model 为 1 时启用", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String expectedResult;


    @Schema(title = "前置条件", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String prerequisite;


    @Schema(title = "备注", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;
}
