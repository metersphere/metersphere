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

@ApiModel(value = "服务集成")
@Table("service_integration")
@Data
public class ServiceIntegration implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{service_integration.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{service_integration.platform.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{service_integration.platform.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "平台", required = true, allowableValues = "range[1, 50]")
    private String platform;


    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, ]")
    private byte[] configuration;


    @ApiModelProperty(name = "工作空间ID", required = false, allowableValues = "range[1, 50]")
    private String workspaceId;


}