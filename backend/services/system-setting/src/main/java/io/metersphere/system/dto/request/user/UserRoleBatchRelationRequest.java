package io.metersphere.system.dto.request.user;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleBatchRelationRequest extends TableBatchProcessDTO {
    /**
     * 权限ID集合
     */
    @Schema(description = "权限ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{organization.id.not_blank}")
    private List<String> roleIds;
}
