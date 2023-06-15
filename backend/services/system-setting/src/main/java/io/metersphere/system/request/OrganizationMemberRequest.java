package io.metersphere.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
public class OrganizationMemberRequest implements Serializable {

    /**
     * 组织ID
     */
    @Schema(title = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.id.not_blank}")
    private String organizationId;

    /**
     * 成员ID集合
     */
    @Schema(title = "成员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{member.id.not_null}")
    private List<String> memberIds;

    /**
     * 创建人ID(组织-添加成员操作)
     */
    @Schema(title = "创建人ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String createUserId;
}
