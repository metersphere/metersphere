package io.metersphere.system.dto.taskcenter.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: LAN
 * @date: 2024/1/17 11:21
 * @version: 1.0
 */
@Data
public class OrganizationTaskCenterPageRequest extends TaskCenterPageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.id.not_blank}")
    @Size(min = 1, max = 50, message = "{organization.id.length_range}")
    private String organizationId;

}
