package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionBatchUpdateRequest extends ApiDefinitionBatchRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所需更新的字段名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "接口状态/进行中/已完成")
    @Size(min = 1, max = 50, message = "{api_definition.status.length_range}")
    private String status;

    @Schema(description = "版本fk")
    @Size(min = 1, max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
            @Size(min = 1, max = 64, message = "{api_test_case.tag.length_range}")
                    String> tags;

    @Schema(description = "自定义字段")
    private ApiDefinitionCustomFieldDTO customField;

    @Schema(description = "是否追加", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean append = false;

    @Schema(description = "是否清空")
    private boolean clear = false;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }

}
