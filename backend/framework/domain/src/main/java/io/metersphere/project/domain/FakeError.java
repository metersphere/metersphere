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

@ApiModel(value = "误报库")
@TableName("fake_error")
@Data
public class FakeError implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 误报ID */
    @TableId
    @NotBlank(message = "误报ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "误报ID")
    private String id;
    
    /** 项目ID */
    @Size(min = 1, max = 50, message = "项目ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "项目ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 创建人 */
    @Size(min = 1, max = 64, message = "创建人长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 更新人 */
    @Size(min = 1, max = 64, message = "更新人长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "更新人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "更新人")
    private String updateUser;
    
    /** 错误码 */
    @Size(min = 1, max = 255, message = "错误码长度必须在1-255之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "错误码不能为空", groups = {Created.class})
    @ApiModelProperty(name = "错误码")
    private String errorCode;
    
    /** 匹配类型 */
    @Size(min = 1, max = 255, message = "匹配类型长度必须在1-255之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "匹配类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "匹配类型")
    private String matchType;
    
    /** 状态 */
    
    
    @ApiModelProperty(name = "状态")
    private Boolean status;
    

}