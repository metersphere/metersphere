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

@ApiModel(value = "自定义函数-代码片段")
@TableName("custom_function")
@Data
public class CustomFunction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** 函数名 */
    @Size(min = 1, max = 255, message = "函数名长度必须在1-255之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "函数名不能为空", groups = {Created.class})
    @ApiModelProperty(name = "函数名")
    private String name;
    
    /** 标签 */
    
    
    @ApiModelProperty(name = "标签")
    private String tags;
    
    /** 函数描述 */
    
    
    @ApiModelProperty(name = "函数描述")
    private String description;
    
    /** 脚本语言类型 */
    @Size(min = 1, max = 255, message = "脚本语言类型长度必须在1-255之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "脚本语言类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "脚本语言类型")
    private String type;
    
    /** 创建人 */
    @Size(min = 1, max = 100, message = "创建人长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 所属项目ID */
    @Size(min = 1, max = 50, message = "所属项目ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "所属项目ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "所属项目ID")
    private String projectId;
    

}