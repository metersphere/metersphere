package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGroupDTO {
    private String userId;
    private String groupId;
    private String sourceId;
    private String name;
    /**
     * 用户组所属类型
     */
    private String type;
}
