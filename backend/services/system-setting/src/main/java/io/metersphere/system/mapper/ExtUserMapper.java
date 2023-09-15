package io.metersphere.system.mapper;

import io.metersphere.sdk.dto.UserExtend;
import io.metersphere.system.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserMapper {

    List<UserExtend> getMemberOption(@Param("sourceId") String sourceId, @Param("keyword") String keyword);

    List<User> getUserListByOrgId(@Param("sourceId") String sourceId, @Param("keyword") String keyword);

    List<User> selectUserList(@Param("keyword") String keyword);

    /**
     * 获取用户组下的用户(组织用户或项目用户)
     *
     * @param ids 用户ID集合
     * @param keyword 关键字
     * @return 用户列表
     */
    List<User> getRoleUserByParam(@Param("ids") List<String> ids, @Param("keyword") String keyword);
}
