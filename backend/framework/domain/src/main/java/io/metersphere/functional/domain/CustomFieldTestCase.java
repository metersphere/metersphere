package io.metersphere.functional.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "自定义字段功能用例关系")
@TableName("custom_field_test_case")
@Data
public class CustomFieldTestCase implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{custom_field_test_case.resource_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "资源ID", required = true, allowableValues = "range[1, 50]")
    private String resourceId;

    @NotBlank(message = "{custom_field_test_case.field_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "字段ID", required = true, allowableValues = "range[1, 50]")
    private String fieldId;


    @ApiModelProperty(name = "字段值", required = false, allowableValues = "range[1, 1000]")
    private String value;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private String textValue;


}