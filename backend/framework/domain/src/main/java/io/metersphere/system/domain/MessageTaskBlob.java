package io.metersphere.system.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
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
@Table("message_task_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class MessageTaskBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{message_task_blob.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "消息模版", required = false, allowableValues = "range[1, ]")
    private String template;


}