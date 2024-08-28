package io.metersphere.plan.dto.response;

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
public class TestPlanApiScenarioPageResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "业务ID")
    private Long num;

    @Schema(description = "场景名称")
    private String name;

    @Schema(description = "场景等级")
    private String priority;

    @Schema(description = "项目id")
    private String projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "环境fk")
    private String environmentId;

    @Schema(description = "环境名称")
    private String environmentName;

    @Schema(description = "执行结果")
    private String lastExecResult;

    @Schema(description = "执行人")
    private String executeUser;

    @Schema(description = "执行人名称")
    private String executeUserName;

    @Schema(description = "最后执行时间")
    private Long lastExecTime;

    @Schema(description = "创建人id")
    private String createUser;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "测试集id")
    private String testPlanCollectionId;

    @Schema(description = "测试集名称")
    private String testPlanCollectionName;

    @Schema(description = "报告id")
    private String lastExecReportId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "场景用例的id")
    private String apiScenarioId;

    @Schema(description = "脚本错误标识")
    private String scriptIdentifier;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "缺陷数量")
    private Integer bugCount;

    @Schema(description = "关联的缺陷数据")
    private List<TestPlanCaseBugDTO> bugList;
}