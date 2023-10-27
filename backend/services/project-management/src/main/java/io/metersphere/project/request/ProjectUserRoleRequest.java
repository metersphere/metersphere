package io.metersphere.project.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目用户组列表请求参数
 *
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUserRoleRequest extends BasePageRequest {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String projectId;
}
