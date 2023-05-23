
package io.metersphere.api.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "mock期望值配置")
@Table("api_definition_mock_config")
@Data
public class ApiDefinitionMockConfig extends ApiDefinitionMock implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition_mock_config.api_definition_mock_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口mock pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @ApiModelProperty(name = "请求内容", required = false, allowableValues = "range[1, ]")
    private byte[] request;

    @ApiModelProperty(name = "响应内容", required = false, allowableValues = "range[1, ]")
    private byte[] response;

}