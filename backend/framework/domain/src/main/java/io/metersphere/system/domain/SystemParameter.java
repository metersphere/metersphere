package io.metersphere.system.domain;

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

@ApiModel(value = "系统参数")
@Table("system_parameter")
@Data
public class SystemParameter implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{system_parameter.param_key.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "参数名称", required = true, allowableValues = "range[1, 64]")
    private String paramKey;


    @ApiModelProperty(name = "参数值", required = false, allowableValues = "range[1, 255]")
    private String paramValue;

    @Size(min = 1, max = 100, message = "{system_parameter.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{system_parameter.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "类型", required = true, allowableValues = "range[1, 100]")
    private String type;


}