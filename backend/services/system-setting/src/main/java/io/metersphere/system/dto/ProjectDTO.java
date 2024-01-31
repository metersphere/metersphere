package io.metersphere.system.dto;

import io.metersphere.project.domain.Project;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectDTO extends Project implements Serializable {
    @Schema(description =  "项目成员数量", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long memberCount;
    @Schema(description = "所属组织", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String organizationName;
    @Schema(description = "管理员", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<UserExtendDTO> adminList;
    @Schema(description = "创建人是否是管理员", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean projectCreateUserIsAdmin;
    @Schema(description =  "模块设置", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> moduleIds;
    @Schema(description =  "资源池", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ProjectResourcePoolDTO> resourcePoolList;
    @Schema(description =  "剩余删除保留天数")
    private Integer remainDayCount;

    private static final long serialVersionUID = 1L;
}
