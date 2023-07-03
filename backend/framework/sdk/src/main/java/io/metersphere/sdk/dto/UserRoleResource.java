package io.metersphere.sdk.dto;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * 权限所属资源，例如 SYSTEM_USER_ROLE
 * @author jianxing
 */
@Data
public class UserRoleResource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Boolean license = false;
}
