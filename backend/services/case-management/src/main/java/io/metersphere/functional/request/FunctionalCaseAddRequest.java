package io.metersphere.functional.request;

import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCaseAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id")
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "模板id")
    @NotBlank(message = "{functional_case.template_id.not_blank}")
    private String templateId;

    @Schema(description = "用例名称")
    @NotBlank(message = "{functional_case.name.not_blank}")
    private String name;

    @Schema(description = "前置条件")
    private String prerequisite;

    @Schema(description = "编辑模式", allowableValues = {"STEP", "TEXT"})
    @NotBlank(message = "{functional_case.case_edit_type.not_blank}")
    private String caseEditType;

    @Schema(description = "用例步骤")
    private String steps;

    @Schema(description = "步骤描述")
    private String textDescription;

    @Schema(description = "预期结果")
    private String expectedResult;

    @Schema(description = "备注")
    private String description;

    @Schema(description = "是否公共用例库")
    private String publicCase;


    @Schema(description = "模块id")
    @NotBlank(message = "{functional_case.module_id.not_blank}")
    private String moduleId;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "标签")
    private String tags;


    @Schema(description = "自定义字段集合")
    private List<CaseCustomsFieldDTO> customsFields;


    @Schema(description = "关联文件ID集合")
    private List<String> relateFileMetaIds = new ArrayList<>();


}
