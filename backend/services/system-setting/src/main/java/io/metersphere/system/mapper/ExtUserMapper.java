package io.metersphere.system.mapper;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.UserExtend;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserMapper {

    List<UserExtend> getMemberOption(String sourceId);

    List<User> getUserListByOrgId(@Param("sourceId") String sourceId);
}
