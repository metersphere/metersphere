
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

@ApiModel(value = "接口同步用例配置")
@TableName("api_sync_config")
@Data
public class ApiSyncConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_sync_config.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{api_sync_config.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "API/CASE 来源fk", required = true, allowableValues = "range[1, 50]")
    private String resourceId;

    @Size(min = 1, max = 50, message = "{api_sync_config.resource_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.resource_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "来源类型/API/CASE", required = true, allowableValues = "range[1, 50]")
    private String resourceType;


    @ApiModelProperty(name = "是否隐藏", required = false, allowableValues = "range[1, 1]")
    private Boolean hide;


    @ApiModelProperty(name = "同步规则", required = false, allowableValues = "range[1, ]")
    private String ruleConfig;

    @Size(min = 1, max = 1, message = "{api_sync_config.notify_case_creator.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.notify_case_creator.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否通知用例创建人", required = true, allowableValues = "range[1, 1]")
    private Boolean notifyCaseCreator;

    @Size(min = 1, max = 1, message = "{api_sync_config.notify_scenario_creator.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.notify_scenario_creator.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否通知场景创建人", required = true, allowableValues = "range[1, 1]")
    private Boolean notifyScenarioCreator;

    @Size(min = 1, max = 1, message = "{api_sync_config.sync_enable.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_sync_config.sync_enable.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否同步用例", required = true, allowableValues = "range[1, 1]")
    private Boolean syncEnable;

    @ApiModelProperty(name = "是否发送通知", required = false, allowableValues = "range[1, 1]")
    private Boolean noticeEnable;

}