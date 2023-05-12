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

@ApiModel(value = "操作日志")
@TableName("operating_log")
@Data
public class OperatingLog implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String id;
    
    /** 项目ID */
    @Size(min = 1, max = 50, message = "项目ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "项目ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /** operating method */
    
    
    @ApiModelProperty(name = "operating method")
    private String operMethod;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 操作人 */
    
    
    @ApiModelProperty(name = "操作人")
    private String operUser;
    
    /** 资源ID */
    
    
    @ApiModelProperty(name = "资源ID")
    private String sourceId;
    
    /** 操作类型 */
    
    
    @ApiModelProperty(name = "操作类型")
    private String operType;
    
    /** 操作模块 */
    
    
    @ApiModelProperty(name = "操作模块")
    private String operModule;
    
    /** 操作标题 */
    
    
    @ApiModelProperty(name = "操作标题")
    private String operTitle;
    
    /** 操作路径 */
    
    
    @ApiModelProperty(name = "操作路径")
    private String operPath;
    
    /** 操作内容 */
    
    
    @ApiModelProperty(name = "操作内容")
    private byte[] operContent;
    
    /** 操作参数 */
    
    
    @ApiModelProperty(name = "操作参数")
    private byte[] operParams;
    
    /** 操作时间 */
    
    
    @ApiModelProperty(name = "操作时间")
    private Long operTime;
    

}