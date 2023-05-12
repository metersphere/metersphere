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
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private Long id;
    
    /** 通知类型 */
    @Size(min = 1, max = 30, message = "通知类型长度必须在1-30之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "通知类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "通知类型")
    private String type;
    
    /** 接收人 */
    @Size(min = 1, max = 50, message = "接收人长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "接收人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "接收人")
    private String receiver;
    
    /** 标题 */
    @Size(min = 1, max = 100, message = "标题长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "标题不能为空", groups = {Created.class})
    @ApiModelProperty(name = "标题")
    private String title;
    
    /** 状态 */
    @Size(min = 1, max = 30, message = "状态长度必须在1-30之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "状态不能为空", groups = {Created.class})
    @ApiModelProperty(name = "状态")
    private String status;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 操作人 */
    @Size(min = 1, max = 50, message = "操作人长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "操作人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "操作人")
    private String operator;
    
    /** 操作 */
    @Size(min = 1, max = 50, message = "操作长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "操作不能为空", groups = {Created.class})
    @ApiModelProperty(name = "操作")
    private String operation;
    
    /** 资源ID */
    @Size(min = 1, max = 50, message = "资源ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "资源ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "资源ID")
    private String resourceId;
    
    /** 资源类型 */
    @Size(min = 1, max = 50, message = "资源类型长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "资源类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "资源类型")
    private String resourceType;
    
    /** 资源名称 */
    @Size(min = 1, max = 100, message = "资源名称长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "资源名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "资源名称")
    private String resourceName;
    

}