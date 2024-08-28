package io.metersphere.plan.dto.response;

import io.metersphere.functional.dto.FunctionalCaseCustomFieldDTO;
import io.metersphere.plan.dto.TestPlanCaseBugDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanCasePageResponse implements Serializable {
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

    @Schema(description = "所属项目")
    private String projectName;

    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "评审状态：未评审/评审中/通过/不通过/重新提审")
    private String reviewStatus;

    @Schema(description = "版本ID")
    private String versionId;

    @Schema(description = "指向初始版本ID")
    private String refId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "标签（JSON)")
    private List<String> tags;

    @Schema(description = "自定义字段集合")
    private List<FunctionalCaseCustomFieldDTO> customFields;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "执行结果")
    private String lastExecResult;

    @Schema(description = "最后执行时间")
    private Long lastExecTime;

    @Schema(description = "执行人")
    private String executeUser;

    @Schema(description = "执行人名称")
    private String executeUserName;

    @Schema(description = "缺陷数量")
    private Integer bugCount;

    @Schema(description = "关联的缺陷数据")
    private List<TestPlanCaseBugDTO> bugList;

    @Schema(description = "用例的id")
    private String caseId;

    @Schema(description = "测试计划id")
    private String testPlanId;

    @Schema(description = "测试集id")
    private String testPlanCollectionId;

    @Schema(description = "测试集名称")
    private String testPlanCollectionName;
}
