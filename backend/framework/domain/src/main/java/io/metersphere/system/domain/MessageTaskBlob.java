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
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "消息通知任务大字段")
@TableName("message_task_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class MessageTaskBlob extends MessageTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{message_task_blob.message_task_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 255]")
    private String messageTaskId;


    @ApiModelProperty(name = "消息模版", required = false, allowableValues = "range[1, ]")
    private String template;


}