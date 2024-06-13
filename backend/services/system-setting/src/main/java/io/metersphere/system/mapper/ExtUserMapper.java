package io.metersphere.system.mapper;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserMapper {

    List<UserExtendDTO> getMemberOption(@Param("sourceId") String sourceId, @Param("keyword") String keyword);

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

    /**
     * 获取某个项目下的固定权限的用户列表
     * @param projectId 项目ID
     * @param keyword 关键字
     * @param permission 权限
     * @return 用户列表
     */
    List<UserDTO>getUserByPermission(@Param("projectId") String projectId, @Param("keyword") String keyword, @Param("permission") String permission);

    List<UserDTO>getUserByKeyword(@Param("projectId") String projectId, @Param("keyword") String keyword);


    long countByIdAndPassword(@Param("userId") String id, @Param("password") String password);

    long updatePasswordByUserId(@Param("userId") String id, @Param("password") String password);

    /**
     * 获取用户的安装时间，兼容历史用户使用问题
     * @return 安装时间
     */
    long gaInstalledTime();

    void updateInstalled();
}
