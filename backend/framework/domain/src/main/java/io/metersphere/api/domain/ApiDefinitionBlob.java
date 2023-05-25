package io.metersphere.api.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口定义详情内容")
@Table("api_definition_blob")
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition_blob.api_definition_id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口fk/ 一对一关系", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Schema(title = "请求内容", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] request;


    @Schema(title = "响应内容", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] response;


    @Schema(title = "备注", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] remark;


}