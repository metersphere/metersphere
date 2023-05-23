
package io.metersphere.api.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "接口定义详情内容")
@Table("api_definition_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class ApiDefinitionBlob extends ApiDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition_blob.api_definition_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口fk/ 一对一关系", required = true, allowableValues = "range[1, 50]")
    private String id;

    @ApiModelProperty(name = "请求内容", required = false, dataType = "byte[]")
    private byte[] request;


    @ApiModelProperty(name = "响应内容", required = false, dataType = "byte[]")
    private byte[] response;


    @ApiModelProperty(name = "备注", required = false, dataType = "byte[]")
    private byte[] remark;


}