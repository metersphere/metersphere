package io.metersphere.functional.request;

import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseAttachmentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "模板id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.template_id.not_blank}")
    private String templateId;

    @Schema(description = "用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.name.not_blank}")
    private String name;

    @Schema(description = "前置条件", defaultValue = "")
    private String prerequisite;

    @Schema(description = "编辑模式", allowableValues = {"STEP", "TEXT"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.case_edit_type.not_blank}")
    private String caseEditType;

    @Schema(description = "用例步骤", defaultValue = "")
    private String steps;

    @Schema(description = "步骤描述", defaultValue = "")
    private String textDescription;

    @Schema(description = "预期结果", defaultValue = "")
    private String expectedResult;

    @Schema(description = "备注", defaultValue = "")
    private String description;

    @Schema(description = "是否公共用例库")
    private String publicCase;

    @Schema(description = "模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.module_id.not_blank}")
    private String moduleId;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "自定义字段集合")
    private List<CaseCustomFieldDTO> customFields;

    @Schema(description = "附件关联文件ID集合")
    private List<String> relateFileMetaIds;

    @Schema(description = "功能用例详情（前置条件/步骤描述/预期结果/备注）上传的文件id集合")
    private List<String> caseDetailFileIds;

    @Schema(description = "评审id")
    private String reviewId;

    @Schema(description = "附件信息")
    private List<FunctionalCaseAttachmentDTO> attachments;

    @Schema(description = "执行结果")
    private String lastExecuteResult;

    @Schema(description = "是否是ai生成的用例")
    private Boolean aiCreate = false;
}
