package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGroupInfoDTO {
    private String userId;
    private String userName;
    private String userEmail;
    private String groupId;
    private String groupName;
}
