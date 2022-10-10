package io.metersphere.request;


import io.metersphere.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserIssueRequest extends UserDTO.PlatformInfo {
    private String workspaceId;
    private String platform;
}
