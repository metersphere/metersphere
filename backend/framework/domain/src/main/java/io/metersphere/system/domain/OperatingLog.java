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

@ApiModel(value = "操作日志")
@TableName("operating_log")
@Data
public class OperatingLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{operating_log.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{operating_log.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{operating_log.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "operating method", required = false, allowableValues = "range[1, 500]")
    private String operMethod;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "操作人", required = false, allowableValues = "range[1, 50]")
    private String operUser;


    @ApiModelProperty(name = "资源ID", required = false, allowableValues = "range[1, 6000]")
    private String sourceId;


    @ApiModelProperty(name = "操作类型", required = false, allowableValues = "range[1, 100]")
    private String operType;


    @ApiModelProperty(name = "操作模块", required = false, allowableValues = "range[1, 100]")
    private String operModule;


    @ApiModelProperty(name = "操作标题", required = false, allowableValues = "range[1, 6000]")
    private String operTitle;


    @ApiModelProperty(name = "操作路径", required = false, allowableValues = "range[1, 500]")
    private String operPath;


    @ApiModelProperty(name = "操作内容", required = false, allowableValues = "range[1, ]")
    private byte[] operContent;


    @ApiModelProperty(name = "操作参数", required = false, allowableValues = "range[1, ]")
    private byte[] operParams;


    @ApiModelProperty(name = "操作时间", required = true, allowableValues = "range[1, ]")
    private Long operTime;


}