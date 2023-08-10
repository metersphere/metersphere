package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class TestResourcePoolBlob implements Serializable {
    @Schema(description =  "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_resource_pool_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_resource_pool_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "资源节点配置")
    private byte[] configuration;

    private static final long serialVersionUID = 1L;
}