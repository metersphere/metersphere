package io.metersphere.project.domain;

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

@ApiModel(value = "自定义字段")
@TableName("custom_field")
@Data
public class CustomField implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 自定义字段ID */
    @TableId
    @NotBlank(message = "自定义字段ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "自定义字段ID")
    private String id;
    
    /** 自定义字段名称 */
    @Size(min = 1, max = 64, message = "自定义字段名称长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "自定义字段名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "自定义字段名称")
    private String name;
    
    /** 使用场景 */
    @Size(min = 1, max = 30, message = "使用场景长度必须在1-30之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "使用场景不能为空", groups = {Created.class})
    @ApiModelProperty(name = "使用场景")
    private String scene;
    
    /** 自定义字段类型 */
    @Size(min = 1, max = 30, message = "自定义字段类型长度必须在1-30之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "自定义字段类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "自定义字段类型")
    private String type;
    
    /** 自定义字段备注 */
    
    
    @ApiModelProperty(name = "自定义字段备注")
    private String remark;
    
    /** 自定义字段选项 */
    
    
    @ApiModelProperty(name = "自定义字段选项")
    private String options;
    
    /** 是否是系统字段 */
    
    
    @ApiModelProperty(name = "是否是系统字段")
    private Boolean system;
    
    /** 是否是全局字段 */
    
    
    @ApiModelProperty(name = "是否是全局字段")
    private Boolean global;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 项目ID */
    
    
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /** 是否关联第三方 */
    @Size(min = 1, max = 1, message = "是否关联第三方长度必须在1-1之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "是否关联第三方不能为空", groups = {Created.class})
    @ApiModelProperty(name = "是否关联第三方")
    private Boolean thirdPart;
    

}