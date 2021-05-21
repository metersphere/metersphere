package io.metersphere.controller.request.member;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddMemberRequest {

    private String workspaceId;
    private List<String> userIds;
    private List<String> roleIds;
    private List<String> groupIds;
    private String projectId;
}
