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

@ApiModel(value = "定时任务")
@TableName("schedule")
@Data
public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** qrtz UUID */
    
    
    @ApiModelProperty(name = "qrtz UUID")
    private String key;
    
    /** 资源类型 */
    @Size(min = 1, max = 50, message = "资源类型长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "资源类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "资源类型")
    private String type;
    
    /** Schedule value */
    @Size(min = 1, max = 255, message = "Schedule value长度必须在1-255之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Schedule value不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Schedule value")
    private String value;
    
    /** Schedule Job Class Name */
    @Size(min = 1, max = 64, message = "Schedule Job Class Name长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Schedule Job Class Name不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Schedule Job Class Name")
    private String job;
    
    /** Schedule Eable */
    
    
    @ApiModelProperty(name = "Schedule Eable")
    private Boolean enable;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String resourceId;
    
    /** 创建人 */
    @Size(min = 1, max = 50, message = "创建人长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** Create timestamp */
    
    
    @ApiModelProperty(name = "Create timestamp")
    private Long createTime;
    
    /** Update timestamp */
    
    
    @ApiModelProperty(name = "Update timestamp")
    private Long updateTime;
    
    /** 项目ID */
    
    
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /** 名称 */
    
    
    @ApiModelProperty(name = "名称")
    private String name;
    
    /** 配置 */
    
    
    @ApiModelProperty(name = "配置")
    private String config;
    

}