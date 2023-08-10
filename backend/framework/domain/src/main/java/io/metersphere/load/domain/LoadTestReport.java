package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReport implements Serializable {
    @Schema(description =  "报告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_report.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.test_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(description =  "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{load_test_report.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "报告描述")
    private String description;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "状态: Starting, Running, Error,Completed etc.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{load_test_report.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "创建人(执行人)ID")
    private String createUser;

    @Schema(description =  "触发方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.trigger_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{load_test_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    private String triggerMode;

    @Schema(description =  "最大并发数")
    private String maxUsers;

    @Schema(description =  "平均响应时间")
    private String avgResponseTime;

    @Schema(description =  "每秒事务数")
    private String tps;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "测试名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.test_name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{load_test_report.test_name.length_range}", groups = {Created.class, Updated.class})
    private String testName;

    @Schema(description =  "资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.test_resource_pool_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    private String testResourcePoolId;

    @Schema(description =  "测试开始时间")
    private Long testStartTime;

    @Schema(description =  "测试结束时间")
    private Long testEndTime;

    @Schema(description =  "执行时长")
    private Long testDuration;

    @Schema(description =  "版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_report.version_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{load_test_report.version_id.length_range}", groups = {Created.class, Updated.class})
    private String versionId;

    @Schema(description =  "关联的测试计划报告ID（可以为空)")
    private String relevanceTestPlanReportId;

    private static final long serialVersionUID = 1L;
}