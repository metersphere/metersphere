package io.metersphere.base.mapper.ext;

import io.metersphere.dto.UserGroupDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserGroupMapper {

    List<UserGroupDTO> getUserGroup(@Param("userId") String userId);
}
