package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.User;
import io.metersphere.controller.request.UserRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtUserMapper {

    List<User> getUserList(@Param("userRequest") UserRequest request);

    int updatePassword(User record);

    String getDefaultLanguage(String paramKey);

    List<User> searchUser(String condition);

    List<String> queryEmailByIds(List<String> userIds);

}
