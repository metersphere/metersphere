package io.metersphere.system.dto;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author guoyuqi
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrgUserExtend extends User {

    @Schema(description =  "项目ID名称集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OptionDTO> projectIdNameMap;;

    @Schema(description =  "用户组ID名称集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OptionDTO> userRoleIdNameMap;
}
