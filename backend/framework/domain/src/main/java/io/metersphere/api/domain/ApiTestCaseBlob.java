package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCaseBlob implements Serializable {
    @Schema(description =  "接口用例pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_test_case_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "请求内容")
    private byte[] request;

    private static final long serialVersionUID = 1L;
}