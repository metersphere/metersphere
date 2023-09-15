package io.metersphere.system.mapper;

import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.dto.UserExcludeOptionDTO;
import io.metersphere.system.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseUserMapper {
    UserDTO selectDTOByKeyword(String keyword);

    UserDTO selectById(String id);

    List<User> findAll();

    void batchSave(@Param("users") List<User> users);

    boolean isSuperUser(String userId);

    String selectEmailInDB(@Param("email") String email, @Param("id") String id);

    List<User> selectUserIdByEmailList(@Param("emailList") List<String> emailList);

    List<User> selectByKeyword(@Param("keyword") String keyword, @Param("selectId") boolean selectId);

    List<String> selectUnDeletedUserIdByIdList(@Param("idList") List<String> userIdList);

    long deleteUser(String id, String deleteUser, long deleteTime);

    List<OptionDTO> selectUserOptionByIds(List<String> userIds);

    List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(@Param("keyword") String keyword);

    List<OptionDTO> getSelectOptionByIdsWithDeleted(List<String> ids);
}
