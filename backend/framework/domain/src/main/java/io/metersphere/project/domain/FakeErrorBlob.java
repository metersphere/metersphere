package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FakeErrorBlob implements Serializable {
    @Schema(title = "Test ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{fake_error_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "内容")
    private byte[] content;

    @Schema(title = "报告内容")
    private byte[] description;

    private static final long serialVersionUID = 1L;
}