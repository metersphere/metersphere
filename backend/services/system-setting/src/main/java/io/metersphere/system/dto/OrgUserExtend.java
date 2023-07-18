package io.metersphere.system.dto;

import io.metersphere.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * @author guoyuqi
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrgUserExtend extends User {

    @Schema(title = "项目ID名称集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String,String> projectIdNameMap;;

    @Schema(title = "用户组ID名称集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String,String> userRoleIdNameMap;
}
