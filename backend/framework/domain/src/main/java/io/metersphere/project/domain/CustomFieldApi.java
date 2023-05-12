package io.metersphere.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "自定义字段关系")
@TableName("custom_field_api")
@Data
public class CustomFieldApi implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{custom_field_api.resource_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "资源ID", required = true, allowableValues = "range[1, 50]")
    private String resourceId;

    @TableId
    @NotBlank(message = "{custom_field_api.field_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "字段ID", required = true, allowableValues = "range[1, 50]")
    private String fieldId;


    @ApiModelProperty(name = "字段值", required = false, allowableValues = "range[1, 500]")
    private String value;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private byte[] textValue;


}