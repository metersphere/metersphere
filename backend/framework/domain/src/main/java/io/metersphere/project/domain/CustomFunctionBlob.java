package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class CustomFunctionBlob implements Serializable {
    @Schema(title = "", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    @NotBlank(message = "{custom_function_blob.function_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{custom_function_blob.function_id.length_range}", groups = {Created.class, Updated.class})
    private String functionId;

    @Schema(title = "参数列表")
    private byte[] params;

    @Schema(title = "函数体")
    private byte[] script;

    @Schema(title = "执行结果")
    private byte[] result;

    private static final long serialVersionUID = 1L;
}