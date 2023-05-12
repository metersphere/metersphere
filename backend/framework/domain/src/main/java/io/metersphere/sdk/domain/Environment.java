package io.metersphere.sdk.domain;

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

@ApiModel(value = "环境")
@TableName("environment")
@Data
public class Environment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{environment.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "Api Test Environment ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 64, message = "{environment.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{environment.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Api Test Environment Name", required = true, allowableValues = "range[1, 64]")
    private String name;

    @Size(min = 1, max = 50, message = "{environment.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{environment.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Project ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "Api Test Protocol", required = false, allowableValues = "range[1, 20]")
    private String protocol;


    @ApiModelProperty(name = "Api Test Socket", required = false, allowableValues = "range[1, 225]")
    private String socket;


    @ApiModelProperty(name = "Api Test Domain", required = false, allowableValues = "range[1, 225]")
    private String domain;


    @ApiModelProperty(name = "Api Test Port", required = false, allowableValues = "range[1, ]")
    private Integer port;


    @ApiModelProperty(name = "Global ariables", required = false, allowableValues = "range[1, ]")
    private String variables;


    @ApiModelProperty(name = "Global Heards", required = false, allowableValues = "range[1, ]")
    private String headers;


    @ApiModelProperty(name = "Config Data (JSON format)", required = false, allowableValues = "range[1, ]")
    private String config;


    @ApiModelProperty(name = "hosts", required = false, allowableValues = "range[1, ]")
    private String hosts;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private Long updateTime;


}