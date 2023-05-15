
package io.metersphere.api.domain;

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
@TableName("api_scenario")
@Data
public class ApiScenario implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_scenario.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 200, message = "{api_scenario.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景名称", required = true, allowableValues = "range[1, 200]")
    private String name;

    @Size(min = 1, max = 50, message = "{api_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "删除时间", required = false, dataType = "Long")
    private Long deleteTime;

    @ApiModelProperty(name = "删除人", required = false, allowableValues = "range[1, 50]")
    private String deleteUser;

    @Size(min = 1, max = 50, message = "{api_scenario.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "更新人", required = true, allowableValues = "range[1, 50]")
    private String updateUser;


    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 10, message = "{api_scenario.level.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.level.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景级别/P0/P1等", required = true, allowableValues = "range[1, 10]")
    private String level;

    @Size(min = 1, max = 20, message = "{api_scenario.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景状态/未规划/已完成 等", required = true, allowableValues = "range[1, 20]")
    private String status;

    @Size(min = 1, max = 50, message = "{api_scenario.principal.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.principal.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "责任人/用户fk", required = true, allowableValues = "range[1, 50]")
    private String principal;


    @ApiModelProperty(name = "场景步骤总数", required = true, dataType = "Integer")
    private Integer stepTotal;

    @ApiModelProperty(name = "通过率", required = true, dataType = "Long")
    private Long passRate;

    @ApiModelProperty(name = "最后一次执行的结果状态", required = false, allowableValues = "range[1, 50]")
    private String reportStatus;

    @ApiModelProperty(name = "编号", required = false, dataType = "Integer")
    private Integer num;

    @ApiModelProperty(name = "自定义id", required = false, allowableValues = "range[1, 50]")
    private String customNum;

    @Size(min = 1, max = 1, message = "{api_scenario.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除状态", required = true, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 1, message = "{api_scenario.environment_group.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.environment_group.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否为环境组", required = true, allowableValues = "range[1, 1]")
    private Boolean environmentGroup;

    @ApiModelProperty(name = "自定义排序", required = true, dataType = "Long")
    private Long pos;

    @ApiModelProperty(name = "版本fk", required = false, allowableValues = "range[1, 50]")
    private String versionId;

    @ApiModelProperty(name = "引用资源fk", required = false, allowableValues = "range[1, 50]")
    private String refId;

    @ApiModelProperty(name = "是否为最新版本 0:否，1:是", required = false, allowableValues = "range[1, 1]")
    private Boolean latest;

    @Size(min = 1, max = 50, message = "{api_scenario.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @ApiModelProperty(name = "场景模块fk", required = false, allowableValues = "range[1, 50]")
    private String apiScenarioModuleId;

    @ApiModelProperty(name = "最后一次执行的报告fk", required = false, allowableValues = "range[1, 50]")
    private String reportId;

    @ApiModelProperty(name = "描述信息", required = false, allowableValues = "range[1, 500]")
    private String description;

    @ApiModelProperty(name = "模块全路径/用于导入模块创建", required = false, allowableValues = "range[1, 1000]")
    private String modulePath;

    @ApiModelProperty(name = "标签", required = false, allowableValues = "range[1, 1000]")
    private String tags;

}