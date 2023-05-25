package io.metersphere.api.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口用例详情")
@Table("api_test_case_blob")
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiTestCaseBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_test_case_blob.api_test_case_id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口用例pk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Schema(title = "请求内容", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] request;

}