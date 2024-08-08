package io.metersphere.functional.xmind.domain;

import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseXmindDTO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "业务ID")
    private String num;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "标签（JSON)")
    private String tags;

    @Schema(description = "编辑模式：步骤模式/文本模式")
    private String caseEditType;

    @Schema(description = "用例步骤（JSON)，step_model 为 Step 时启用")
    private String steps;

    @Schema(description = "步骤描述，step_model 为 Text 时启用")
    private String textDescription;

    @Schema(description = "预期结果，step_model 为 Text  时启用")
    private String expectedResult;

    @Schema(description = "前置条件")
    private String prerequisite;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "自定义字段")
    private List<FunctionalCaseCustomField> customFieldDTOList;

    @Schema(description = "模板自定义字段")
    private List<TemplateCustomFieldDTO> templateCustomFieldDTOList;
}
