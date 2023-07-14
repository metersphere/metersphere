package io.metersphere.sdk.mapper;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.system.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseUserMapper {
    UserDTO selectByEmail(String email);

    UserDTO selectById(String id);

    List<User> findAll();

    void batchSave(@Param("users") List<User> users);

    boolean isSuperUser(String userId);

    String selectEmailInDB(@Param("email") String email, @Param("id") String id);

    List<User> selectUserIdByEmailList(@Param("emailList") List<String> emailList);

    List<User> selectByKeyword(String keyword);

    List<String> selectUnDeletedUserIdByIdList(@Param("idList") List<String> userIdList);

    List<User> selectUserByIdList(List<String> userIds);
}
