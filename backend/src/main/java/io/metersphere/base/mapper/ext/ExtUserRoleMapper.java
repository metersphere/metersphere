package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.User;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.dto.UserRoleHelpDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserRoleMapper {

    List<UserRoleHelpDTO> getUserRoleHelpList(@Param("userId") String userId);

    List<User> getMemberList(@Param("member") QueryMemberRequest request);
}
