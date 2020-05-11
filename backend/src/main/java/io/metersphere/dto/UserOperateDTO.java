package io.metersphere.dto;

import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserOperateDTO extends User {

    private List<Role> roleList;

}
