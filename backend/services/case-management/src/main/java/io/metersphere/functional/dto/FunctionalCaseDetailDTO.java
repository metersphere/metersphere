package io.metersphere.functional.dto;

import io.metersphere.system.dto.sdk.OptionDTO;
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
    private Long num;

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
    private List<String> tags;

    @Schema(description = "编辑模式")
    private String caseEditType;

    @Schema(description = "版本")
    private String versionId;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "是否是公共用例库")
    private Boolean publicCase;

    @Schema(description = "是否是最新版")
    private Boolean latest;

    @Schema(description = "是否是ai自动生成的用例")
    private Boolean aiCreate;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "创建人姓名")
    private String createUserName;

    @Schema(description =  "创建时间")
    private Long createTime;

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

    @Schema(description = "用例等级")
    private String functionalPriority;

    @Schema(description = "需求数量")
    private Integer demandCount;

    @Schema(description = "用例数量")
    private Integer caseCount;

    @Schema(description = "缺陷数量")
    private Integer bugCount;

    @Schema(description = "依赖关系数量")
    private Integer relateEdgeCount;

    @Schema(description = "用例评审数量")
    private Integer caseReviewCount;

    @Schema(description = "测试计划数量")
    private Integer testPlanCount;

    @Schema(description = "评论总数量")
    private Integer commentCount;

    @Schema(description = "各种评论数量集合")
    private List<OptionDTO> commentList;

    @Schema(description = "变更历史数量")
    private Integer historyCount;

    @Schema(description = "执行结果")
    private String lastExecuteResult;
}
