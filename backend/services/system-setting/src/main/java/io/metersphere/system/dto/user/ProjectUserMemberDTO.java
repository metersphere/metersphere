package io.metersphere.system.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUserMemberDTO {

    @Schema(description = "用户id")
    private String id;
    @Schema(description = "项目id")
    private String projectId;
    @Schema(description = "用户名称")
    private String name;
}
