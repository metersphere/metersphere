package io.metersphere.controller.request.organization;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddOrgMemberRequest {

    private String organizationId;
    private List<String> userIds;
    private List<String> roleIds;
    private List<String> groupIds;
}
