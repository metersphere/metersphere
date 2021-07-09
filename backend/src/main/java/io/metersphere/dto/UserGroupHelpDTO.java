package io.metersphere.dto;

import lombok.Data;

@Data
public class UserGroupHelpDTO {
    private String groupId;
    private String groupName;
    private String sourceId;
    private String sourceName;
    private String parentId;
}
