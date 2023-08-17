package io.metersphere.sdk.dto;

import io.metersphere.project.domain.Project;
import io.metersphere.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectDTO extends Project {
    @Schema(description =  "项目成员数量", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long memberCount;
    @Schema(description =  "所属组织", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String organizationName;
    @Schema(description =  "管理员", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<User> adminList;
    @Schema(description =  "创建人是否是管理员", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean projectCreateUserIsAdmin;
}
