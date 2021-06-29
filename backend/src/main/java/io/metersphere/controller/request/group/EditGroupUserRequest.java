package io.metersphere.controller.request.group;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditGroupUserRequest {
    private List<String> userIds;
    private List<String> sourceIds;
    private String groupId;
}
