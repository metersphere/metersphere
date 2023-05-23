package io.metersphere.project.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "文件基础信息")
@Table("file_metadata")
@Data
public class FileMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{file_metadata.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "文件ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 250, message = "{file_metadata.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{file_metadata.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "文件名", required = true, allowableValues = "range[1, 250]")
    private String name;


    @ApiModelProperty(name = "文件类型", required = false, allowableValues = "range[1, 64]")
    private String type;


    @ApiModelProperty(name = "文件大小", required = true, allowableValues = "range[1, ]")
    private Long size;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "项目ID", required = false, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{file_metadata.storage.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{file_metadata.storage.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "文件存储方式", required = true, allowableValues = "range[1, 50]")
    private String storage;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "修改人", required = false, allowableValues = "range[1, 100]")
    private String updateUser;


    @ApiModelProperty(name = "标签", required = false, allowableValues = "range[1, 2000]")
    private String tags;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 255]")
    private String description;


    @ApiModelProperty(name = "文件所属模块", required = false, allowableValues = "range[1, 50]")
    private String moduleId;


    @ApiModelProperty(name = "是否加载jar（开启后用于接口测试执行时使用）", required = false, allowableValues = "range[1, 1]")
    private Boolean loadJar;


    @ApiModelProperty(name = "文件存储路径", required = false, allowableValues = "range[1, 1000]")
    private String path;


    @ApiModelProperty(name = "资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据", required = false, allowableValues = "range[1, 50]")
    private String resourceType;


    @ApiModelProperty(name = "是否是最新版", required = false, allowableValues = "range[1, 1]")
    private Boolean latest;


    @ApiModelProperty(name = "同版本数据关联的ID", required = false, allowableValues = "range[1, 50]")
    private String refId;


}