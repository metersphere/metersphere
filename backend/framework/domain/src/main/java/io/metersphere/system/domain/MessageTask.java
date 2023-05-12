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
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** 消息类型 */
    @Size(min = 1, max = 50, message = "消息类型长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "消息类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "消息类型")
    private String type;
    
    /** 通知事件类型 */
    @Size(min = 1, max = 255, message = "通知事件类型长度必须在1-255之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "通知事件类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "通知事件类型")
    private String event;
    
    /** 接收人id */
    @Size(min = 1, max = 50, message = "接收人id长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "接收人id不能为空", groups = {Created.class})
    @ApiModelProperty(name = "接收人id")
    private String receiver;
    
    /** 任务类型 */
    @Size(min = 1, max = 64, message = "任务类型长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "任务类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "任务类型")
    private String taskType;
    
    /** webhook地址 */
    
    
    @ApiModelProperty(name = "webhook地址")
    private String webhook;
    
    /** 具体测试的ID */
    @Size(min = 1, max = 255, message = "具体测试的ID长度必须在1-255之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "具体测试的ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "具体测试的ID")
    private String testId;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 项目ID */
    @Size(min = 1, max = 64, message = "项目ID长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "项目ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    

}