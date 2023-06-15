package io.metersphere.system.dto;

import io.metersphere.system.domain.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserExtend extends User {

    /**
     * 是否管理员(组织, 项目)
     */
    private boolean adminFlag;
}
