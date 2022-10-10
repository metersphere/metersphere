package io.metersphere.reportstatistics.dto.response;

import io.metersphere.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserGroupResponse {
    private String groupName;
    List<UserDTO> users;
}
