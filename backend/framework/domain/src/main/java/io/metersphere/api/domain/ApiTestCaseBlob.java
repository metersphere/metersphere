package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCaseBlob implements Serializable {
    @Schema(title = "接口用例pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case_blob.id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{api_test_case_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "请求内容")
    private byte[] request;

    private static final long serialVersionUID = 1L;
}