package io.metersphere.api.dto.definition.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * @author lan
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDefinitionMockUpdateRequest extends ApiDefinitionMockAddRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口 mock pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition_mock.id.length_range}")
    private String id;

    /**
     * 删除本地上传的文件ID
     */
    @Schema(description = "删除的文件ID")
    private List<String> deleteFileIds;

    /**
     * 删除关联的文件ID
     */
    @Schema(description = "取消关联文件ID")
    private List<String> unLinkFileIds;

}
