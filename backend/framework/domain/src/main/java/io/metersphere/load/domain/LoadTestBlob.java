package io.metersphere.load.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestBlob implements Serializable {
    @Schema(description =  "测试ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{load_test_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{load_test_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "压力配置")
    private byte[] loadConfiguration;

    @Schema(description =  "高级配置")
    private byte[] advancedConfiguration;

    @Schema(description =  "环境信息 (JSON format)")
    private byte[] envInfo;

    private static final long serialVersionUID = 1L;
}