package io.metersphere.api.dto.definition;

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
public class ApiDefinitionUpdateRequest extends ApiDefinitionAddRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition.id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_definition.id.length_range}")
    private String id;

    /**
     * 新上传的文件ID
     * 为了解决文件名称重复的问题，需要把文件和ID一一对应
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID，与上传的文件顺序保持一致")
    private List<String> addFileIds;
}
