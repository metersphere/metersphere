package io.metersphere.system.dto;

import io.metersphere.system.domain.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserExtend extends User implements Serializable {

    /**
     * 是否管理员(组织, 项目)
     */
    private boolean adminFlag;

    /**
     * 是否成员(组织, 项目)
     */
    private boolean memberFlag;

    private static final long serialVersionUID = 1L;
}
