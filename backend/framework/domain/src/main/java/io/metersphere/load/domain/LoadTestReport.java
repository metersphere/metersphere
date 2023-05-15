package io.metersphere.load.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;

@ApiModel(value = "性能报告")
@TableName("load_test_report")
@Data
public class LoadTestReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{load_test_report.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{load_test_report.test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.test_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试ID", required = true, allowableValues="range[1, 50]")
    private String testId;
    
    @Size(min = 1, max = 64, message = "{load_test_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告名称", required = true, allowableValues="range[1, 64]")
    private String name;
    
    
    @ApiModelProperty(name = "报告描述", required = false, allowableValues="range[1, 500]")
    private String description;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    
    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;
    
    @Size(min = 1, max = 64, message = "{load_test_report.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "状态: Starting, Running, Error,Completed etc.", required = true, allowableValues="range[1, 64]")
    private String status;
    
    @Size(min = 1, max = 64, message = "{load_test_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人(执行人)ID", required = true, allowableValues="range[1, 64]")
    private String createUser;
    
    @Size(min = 1, max = 64, message = "{load_test_report.trigger_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.trigger_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "触发方式", required = true, allowableValues="range[1, 64]")
    private String triggerMode;
    
    
    @ApiModelProperty(name = "最大并发数", required = false, allowableValues="range[1, 10]")
    private String maxUsers;
    
    
    @ApiModelProperty(name = "平均响应时间", required = false, allowableValues="range[1, 10]")
    private String avgResponseTime;
    
    
    @ApiModelProperty(name = "每秒事务数", required = false, allowableValues="range[1, 10]")
    private String tps;
    
    @Size(min = 1, max = 50, message = "{load_test_report.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;
    
    @Size(min = 1, max = 64, message = "{load_test_report.test_name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.test_name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试名称", required = true, allowableValues="range[1, 64]")
    private String testName;
    
    @Size(min = 1, max = 50, message = "{load_test_report.test_resource_pool_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.test_resource_pool_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源池ID", required = true, allowableValues="range[1, 50]")
    private String testResourcePoolId;
    
    
    @ApiModelProperty(name = "测试开始时间", required = false, dataType = "Long")
    private Long testStartTime;
    
    
    @ApiModelProperty(name = "测试结束时间", required = false, dataType = "Long")
    private Long testEndTime;
    
    
    @ApiModelProperty(name = "执行时长", required = false, dataType = "Long")
    private Long testDuration;
    
    @Size(min = 1, max = 50, message = "{load_test_report.version_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report.version_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "版本ID", required = true, allowableValues="range[1, 50]")
    private String versionId;
    
    
    @ApiModelProperty(name = "关联的测试计划报告ID（可以为空)", required = false, allowableValues="range[1, 50]")
    private String relevanceTestPlanReportId;
    

}