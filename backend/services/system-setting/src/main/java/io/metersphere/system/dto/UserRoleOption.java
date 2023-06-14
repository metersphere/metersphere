package io.metersphere.system.dto;

import lombok.Data;

@Data
public class UserRoleOption {
    private String id;
    private String name;
    private boolean selected = false;
    private boolean closeable = true;
}
