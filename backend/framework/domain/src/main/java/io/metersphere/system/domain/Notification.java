package io.metersphere.system.domain;

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

@ApiModel(value = "消息通知")
@TableName("notification")
@Data
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{notification.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, ]")
    private Long id;

    @Size(min = 1, max = 30, message = "{notification.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "通知类型", required = true, allowableValues = "range[1, 30]")
    private String type;

    @Size(min = 1, max = 50, message = "{notification.receiver.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.receiver.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接收人", required = true, allowableValues = "range[1, 50]")
    private String receiver;

    @Size(min = 1, max = 100, message = "{notification.title.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.title.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "标题", required = true, allowableValues = "range[1, 100]")
    private String title;

    @Size(min = 1, max = 30, message = "{notification.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "状态", required = true, allowableValues = "range[1, 30]")
    private String status;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{notification.operator.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.operator.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "操作人", required = true, allowableValues = "range[1, 50]")
    private String operator;

    @Size(min = 1, max = 50, message = "{notification.operation.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.operation.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "操作", required = true, allowableValues = "range[1, 50]")
    private String operation;

    @Size(min = 1, max = 50, message = "{notification.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源ID", required = true, allowableValues = "range[1, 50]")
    private String resourceId;

    @Size(min = 1, max = 50, message = "{notification.resource_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.resource_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源类型", required = true, allowableValues = "range[1, 50]")
    private String resourceType;

    @Size(min = 1, max = 100, message = "{notification.resource_name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{notification.resource_name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源名称", required = true, allowableValues = "range[1, 100]")
    private String resourceName;


}