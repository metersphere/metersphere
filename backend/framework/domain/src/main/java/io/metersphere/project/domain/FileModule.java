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

    @TableId
    @NotBlank(message = "{file_module.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{file_module.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{file_module.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 64, message = "{file_module.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{file_module.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模块名称", required = true, allowableValues = "range[1, 64]")
    private String name;


    @ApiModelProperty(name = "父级ID", required = false, allowableValues = "range[1, 50]")
    private String parentId;


    @ApiModelProperty(name = "层数", required = false, allowableValues = "range[1, ]")
    private Integer level;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "排序用的标识", required = false, allowableValues = "range[1, 22]")
    private Double pos;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "模块类型: module/repository", required = false, allowableValues = "range[1, 20]")
    private String moduleType;


}