package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleDTO {

    private String id;
    private String roleId;
    private String type;
    private String name;
    private String desc;
    private String parentId;
    private Boolean switchable = true;

}
