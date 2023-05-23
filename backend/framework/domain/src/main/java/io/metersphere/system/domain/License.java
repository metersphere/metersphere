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

@ApiModel(value = "")
@Table("license")
@Data
public class License implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
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