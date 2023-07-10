package io.metersphere.sdk.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class OperationLogBlob implements Serializable {
    @Schema(title = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_log_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{operation_log_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "变更前内容")
    private byte[] originalValue;

    @Schema(title = "变更后内容")
    private byte[] modifiedValue;

    private static final long serialVersionUID = 1L;
}