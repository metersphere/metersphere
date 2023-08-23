package io.metersphere.system.dto;

import io.metersphere.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserExtend extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否管理员(组织, 项目)
     */
    @Schema(description =  "是否组织/项目管理员, 是: 展示管理员标识, 否: 不展示管理员标识")
    private boolean adminFlag;

    /**
     * 是否成员(组织, 项目)
     */
    @Schema(description =  "是否组织/项目成员, 是: 勾选禁用, 否: 勾选启用")
    private boolean memberFlag;

    /**
     * 是否勾选用户组
     */
    @Schema(description =  "是否属于用户组, 是: 勾选禁用, 否: 勾选启用")
    private boolean checkRoleFlag;
}
