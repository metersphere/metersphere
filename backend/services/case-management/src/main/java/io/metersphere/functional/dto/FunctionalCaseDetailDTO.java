package io.metersphere.functional.dto;

import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "业务ID")
    private Integer num;

    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "用例名称")
    private String name;

    @Schema(description = "评审状态")
    private String reviewStatus;

    @Schema(description =  "标签（JSON)")
    private String tags;

    @Schema(description = "编辑模式")
    private String caseEditType;

    @Schema(description = "版本")
    private String versionId;

    @Schema(description = "是否是公共用例库")
    private Boolean publicCase;

    @Schema(description = "是否是最新版")
    private Boolean latest;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "用例步骤（JSON)，step_model 为 Step 时启用")
    private String steps;

    @Schema(description =  "步骤描述，step_model 为 Text 时启用")
    private String textDescription;

    @Schema(description =  "预期结果，step_model 为 Text  时启用")
    private String expectedResult;

    @Schema(description =  "前置条件")
    private String prerequisite;

    @Schema(description =  "备注")
    private String description;

    @Schema(description = "自定义字段属性")
    private List<TemplateCustomFieldDTO> customFields;

    @Schema(description = "附件信息")
    private List<FunctionalCaseAttachmentDTO> attachments;

    @Schema(description = "关注标识")
    private Boolean followFlag;
}
