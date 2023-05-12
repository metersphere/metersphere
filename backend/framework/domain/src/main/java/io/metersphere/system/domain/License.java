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

@ApiModel(value = "")
@TableName("license")
@Data
public class License implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{license.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "Create timestamp", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "Update timestamp", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "license_code", required = false, allowableValues = "range[1, ]")
    private String licenseCode;


}