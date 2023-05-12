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

@ApiModel(value = "三方认证源")
@TableName("auth_source")
@Data
public class AuthSource implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{auth_source.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "认证源ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "认证源配置", required = true, allowableValues = "range[1, ]")
    private byte[] configuration;

    @Size(min = 1, max = 64, message = "{auth_source.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{auth_source.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "状态 启用 禁用", required = true, allowableValues = "range[1, 64]")
    private String status;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 255]")
    private String description;


    @ApiModelProperty(name = "名称", required = false, allowableValues = "range[1, 60]")
    private String name;


    @ApiModelProperty(name = "类型", required = false, allowableValues = "range[1, 30]")
    private String type;


}