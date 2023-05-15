package io.metersphere.api.domain;

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

@ApiModel(value = "接口定义")
@TableName("api_definition")
@Data
public class ApiDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_definition.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口pk", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 100, message = "{api_definition.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "修改时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 64, message = "{api_definition.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "修改人", required = true, allowableValues = "range[1, 64]")
    private String updateUser;


    @ApiModelProperty(name = "删除人", required = false, allowableValues = "range[1, 64]")
    private String deleteUser;


    @ApiModelProperty(name = "删除时间", required = false, dataType = "Long")
    private Long deleteTime;

    @Size(min = 1, max = 1, message = "{api_definition.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "删除状态", required = true, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 255, message = "{api_definition.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口名称", required = true, allowableValues = "range[1, 255]")
    private String name;

    @Size(min = 1, max = 64, message = "{api_definition.method.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.method.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口类型", required = true, allowableValues = "range[1, 64]")
    private String method;

    @Size(min = 1, max = 255, message = "{api_definition.protocol.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.protocol.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口协议", required = true, allowableValues = "range[1, 255]")
    private String protocol;


    @ApiModelProperty(name = "接口路径-只有HTTP协议有值", required = false, allowableValues = "range[1, 255]")
    private String path;


    @ApiModelProperty(name = "模块全路径-用于导入处理", required = false, allowableValues = "range[1, 1000]")
    private String modulePath;

    @Size(min = 1, max = 64, message = "{api_definition.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口状态/进行中/已完成", required = true, allowableValues = "range[1, 64]")
    private String status;


    @ApiModelProperty(name = "模块fk", required = false, allowableValues = "range[1, 50]")
    private String moduleId;


    @ApiModelProperty(name = "自定义id", required = false, dataType = "Integer")
    private Integer num;


    @ApiModelProperty(name = "标签", required = false, allowableValues = "range[1, 1000]")
    private String tags;


    @ApiModelProperty(name = "自定义排序", required = true, dataType = "Long")
    private Long pos;

    @Size(min = 1, max = 1, message = "{api_definition.sync_enable.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.sync_enable.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否启用同步", required = true, allowableValues = "range[1, 1]")
    private Boolean syncEnable;


    @ApiModelProperty(name = "同步开始时间", required = false, dataType = "Long")
    private Long syncTime;

    @Size(min = 1, max = 50, message = "{api_definition.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "环境fk", required = false, allowableValues = "range[1, 50]")
    private String environmentId;

    @Size(min = 1, max = 1, message = "{api_definition.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition.latest.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否为最新版本 0:否，1:是", required = true, allowableValues = "range[1, 1]")
    private Boolean latest;


    @ApiModelProperty(name = "版本fk", required = false, allowableValues = "range[1, 50]")
    private String versionId;


    @ApiModelProperty(name = "版本引用fk", required = false, allowableValues = "range[1, 50]")
    private String refId;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 500]")
    private String description;


}