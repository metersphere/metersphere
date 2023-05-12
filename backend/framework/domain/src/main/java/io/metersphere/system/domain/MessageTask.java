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

@ApiModel(value = "消息通知任务")
@TableName("message_task")
@Data
public class MessageTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{message_task.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 255]")
    private String id;

    @Size(min = 1, max = 50, message = "{message_task.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{message_task.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "消息类型", required = true, allowableValues = "range[1, 50]")
    private String type;

    @Size(min = 1, max = 255, message = "{message_task.event.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{message_task.event.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "通知事件类型", required = true, allowableValues = "range[1, 255]")
    private String event;

    @Size(min = 1, max = 50, message = "{message_task.receiver.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{message_task.receiver.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接收人id", required = true, allowableValues = "range[1, 50]")
    private String receiver;

    @Size(min = 1, max = 64, message = "{message_task.task_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{message_task.task_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "任务类型", required = true, allowableValues = "range[1, 64]")
    private String taskType;


    @ApiModelProperty(name = "webhook地址", required = false, allowableValues = "range[1, 255]")
    private String webhook;

    @Size(min = 1, max = 255, message = "{message_task.test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{message_task.test_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "具体测试的ID", required = true, allowableValues = "range[1, 255]")
    private String testId;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;

    @Size(min = 1, max = 64, message = "{message_task.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{message_task.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 64]")
    private String projectId;


}