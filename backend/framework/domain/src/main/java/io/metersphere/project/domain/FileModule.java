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

@ApiModel(value = "文件管理模块")
@TableName("file_module")
@Data
public class FileModule implements Serializable {
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
    
    /** 模块名称 */
    @Size(min = 1, max = 64, message = "模块名称长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "模块名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "模块名称")
    private String name;
    
    /** 父级ID */
    
    
    @ApiModelProperty(name = "父级ID")
    private String parentId;
    
    /** 层数 */
    
    
    @ApiModelProperty(name = "层数")
    private Integer level;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 排序用的标识 */
    
    
    @ApiModelProperty(name = "排序用的标识")
    private Double pos;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 模块类型: module/repository */
    
    
    @ApiModelProperty(name = "模块类型: module/repository")
    private String moduleType;
    

}