package io.metersphere.track.request.testcase;


import io.metersphere.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserIssueRequest extends UserDTO.PlatformInfo {
    private String orgId;
    private String platform;
}
