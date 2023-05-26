package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionBlob implements Serializable {
    @Schema(title = "接口fk/ 一对一关系", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_blob.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "请求内容")
    private byte[] request;

    @Schema(title = "响应内容")
    private byte[] response;

    @Schema(title = "备注")
    private byte[] remark;

    private static final long serialVersionUID = 1L;
}