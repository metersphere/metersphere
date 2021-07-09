package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserGroupPermission implements Serializable {
    private String id;

    private String groupId;

    private String permissionId;

    private String moduleId;

    private static final long serialVersionUID = 1L;
}