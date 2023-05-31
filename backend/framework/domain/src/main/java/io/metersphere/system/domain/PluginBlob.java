package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class PluginBlob implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{plugin_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{plugin_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "plugin form option")
    private byte[] formOption;

    @Schema(title = "plugin form script")
    private byte[] formScript;

    private static final long serialVersionUID = 1L;
}