package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: LAN
 * @date: 2024/1/3 16:40
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OperationHistoryVersionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "变更记录id")
    @NotNull(message = "{operation_history.id.not_blank}")
    private Long id;

    @Schema(description = "资源id（当前变更记录的资源id）")
    @NotBlank(message = "{operation_history.source_id.not_blank}")
    private String sourceId;

    @Schema(description = "版本id")
    @NotBlank(message = "{operation_history.version_id.not_blank}")
    @Size(min = 1, max = 50, message = "{operation_history.version_id.length_range}")
    private String versionId;

}

