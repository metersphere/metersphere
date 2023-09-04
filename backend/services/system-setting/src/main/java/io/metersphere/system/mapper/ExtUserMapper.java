package io.metersphere.system.mapper;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.UserExtend;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserMapper {

    List<UserExtend> getMemberOption(@Param("sourceId") String sourceId, @Param("keyword") String keyword);

    List<User> getUserListByOrgId(@Param("sourceId") String sourceId);

    List<User> selectUserList(@Param("keyword") String keyword);
}
