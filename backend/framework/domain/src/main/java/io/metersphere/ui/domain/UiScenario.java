package io.metersphere.ui.domain;

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

@ApiModel(value = "场景")
@TableName("ui_scenario")
@Data
public class UiScenario implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{ui_scenario.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "场景ID", required = true, allowableValues="range[1, 255]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{ui_scenario.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;
    
    
    @ApiModelProperty(name = "标签", required = false, allowableValues="range[1, 2000]")
    private String tags;
    
    @Size(min = 1, max = 64, message = "{ui_scenario.module_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.module_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模块ID", required = true, allowableValues="range[1, 64]")
    private String moduleId;
    
    @Size(min = 1, max = 1000, message = "{ui_scenario.module_path.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.module_path.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模块路径", required = true, allowableValues="range[1, 1000]")
    private String modulePath;
    
    @Size(min = 1, max = 255, message = "{ui_scenario.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景名称", required = true, allowableValues="range[1, 255]")
    private String name;
    
    @Size(min = 1, max = 100, message = "{ui_scenario.level.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.level.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用例等级", required = true, allowableValues="range[1, 100]")
    private String level;
    
    @Size(min = 1, max = 100, message = "{ui_scenario.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "状态", required = true, allowableValues="range[1, 100]")
    private String status;
    
    @Size(min = 1, max = 100, message = "{ui_scenario.principal.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.principal.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "责任人", required = true, allowableValues="range[1, 100]")
    private String principal;
    
    
    @ApiModelProperty(name = "步骤总数", required = true, dataType = "Integer")
    private Integer stepTotal;
    
    
    @ApiModelProperty(name = "定时任务的表达式", required = false, allowableValues="range[1, 255]")
    private String schedule;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    
    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;
    
    
    @ApiModelProperty(name = "最后执行结果", required = false, allowableValues="range[1, 100]")
    private String lastResult;
    
    
    @ApiModelProperty(name = "最后执行结果的报告ID", required = false, allowableValues="range[1, 50]")
    private String reportId;
    
    
    @ApiModelProperty(name = "num", required = true, dataType = "Integer")
    private Integer num;
    
    @Size(min = 1, max = 1, message = "{ui_scenario.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除状态", required = true, allowableValues="range[1, 1]")
    private Boolean deleted;
    
    
    @ApiModelProperty(name = "自定义num", required = false, allowableValues="range[1, 64]")
    private String customNum;
    
    @Size(min = 1, max = 100, message = "{ui_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 100]")
    private String createUser;
    
    
    @ApiModelProperty(name = "删除时间", required = false, dataType = "Long")
    private Long deleteTime;
    
    
    @ApiModelProperty(name = "删除人", required = false, allowableValues="range[1, 64]")
    private String deleteUser;
    
    
    @ApiModelProperty(name = "执行次数", required = true, dataType = "Integer")
    private Integer executeTimes;
    
    
    @ApiModelProperty(name = "自定义排序，间隔5000", required = true, dataType = "Long")
    private Long pos;
    
    
    @ApiModelProperty(name = "环境类型（环境，环境组）", required = false, allowableValues="range[1, 20]")
    private String environmentType;
    
    
    @ApiModelProperty(name = "环境组ID", required = false, allowableValues="range[1, 50]")
    private String environmentGroupId;
    
    @Size(min = 1, max = 50, message = "{ui_scenario.version_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.version_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "版本ID", required = true, allowableValues="range[1, 50]")
    private String versionId;
    
    @Size(min = 1, max = 255, message = "{ui_scenario.ref_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.ref_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "指向初始版本ID", required = true, allowableValues="range[1, 255]")
    private String refId;
    
    @Size(min = 1, max = 1, message = "{ui_scenario.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_scenario.latest.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否为最新版本 0:否，1:是", required = true, allowableValues="range[1, 1]")
    private Boolean latest;
    

}