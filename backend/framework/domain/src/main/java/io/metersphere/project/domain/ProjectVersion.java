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

@ApiModel(value = "版本管理")
@TableName("project_version")
@Data
public class ProjectVersion implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 版本ID */
    @TableId
    @NotBlank(message = "版本ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "版本ID")
    private String id;
    
    /** 项目ID */
    @Size(min = 1, max = 50, message = "项目ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "项目ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /** 版本名称 */
    @Size(min = 1, max = 100, message = "版本名称长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "版本名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "版本名称")
    private String name;
    
    /** 描述 */
    
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    /** 状态 */
    
    
    @ApiModelProperty(name = "状态")
    private String status;
    
    /** 是否是最新版 */
    @Size(min = 1, max = 1, message = "是否是最新版长度必须在1-1之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "是否是最新版不能为空", groups = {Created.class})
    @ApiModelProperty(name = "是否是最新版")
    private Boolean latest;
    
    /** 发布时间 */
    
    
    @ApiModelProperty(name = "发布时间")
    private Long publishTime;
    
    /** 开始时间 */
    
    
    @ApiModelProperty(name = "开始时间")
    private Long startTime;
    
    /** 结束时间 */
    
    
    @ApiModelProperty(name = "结束时间")
    private Long endTime;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 创建人 */
    @Size(min = 1, max = 100, message = "创建人长度必须在1-100之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人")
    private String createUser;
    

}