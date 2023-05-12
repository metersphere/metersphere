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

@ApiModel(value = "文件基础信息")
@TableName("file_metadata")
@Data
public class FileMetadata implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 文件ID */
    @TableId
    @NotBlank(message = "文件ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "文件ID")
    private String id;
    
    /** 文件名 */
    @Size(min = 1, max = 250, message = "文件名长度必须在1-250之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "文件名不能为空", groups = {Created.class})
    @ApiModelProperty(name = "文件名")
    private String name;
    
    /** 文件类型 */
    
    
    @ApiModelProperty(name = "文件类型")
    private String type;
    
    /** 文件大小 */
    
    
    @ApiModelProperty(name = "文件大小")
    private Long size;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 项目ID */
    
    
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /** 文件存储方式 */
    @Size(min = 1, max = 50, message = "文件存储方式长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "文件存储方式不能为空", groups = {Created.class})
    @ApiModelProperty(name = "文件存储方式")
    private String storage;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 修改人 */
    
    
    @ApiModelProperty(name = "修改人")
    private String updateUser;
    
    /** 标签 */
    
    
    @ApiModelProperty(name = "标签")
    private String tags;
    
    /** 描述 */
    
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    /** 文件所属模块 */
    
    
    @ApiModelProperty(name = "文件所属模块")
    private String moduleId;
    
    /** 是否加载jar（开启后用于接口测试执行时使用） */
    
    
    @ApiModelProperty(name = "是否加载jar（开启后用于接口测试执行时使用）")
    private Boolean loadJar;
    
    /** 文件存储路径 */
    
    
    @ApiModelProperty(name = "文件存储路径")
    private String path;
    
    /** 资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据 */
    
    
    @ApiModelProperty(name = "资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据")
    private String resourceType;
    
    /** 是否是最新版 */
    
    
    @ApiModelProperty(name = "是否是最新版")
    private Boolean latest;
    
    /** 同版本数据关联的ID */
    
    
    @ApiModelProperty(name = "同版本数据关联的ID")
    private String refId;
    

}