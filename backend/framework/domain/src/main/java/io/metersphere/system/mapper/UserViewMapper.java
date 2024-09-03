package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserView;
import io.metersphere.system.domain.UserViewExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserViewMapper {
    long countByExample(UserViewExample example);

    int deleteByExample(UserViewExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserView record);

    int insertSelective(UserView record);

    List<UserView> selectByExample(UserViewExample example);

    UserView selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserView record, @Param("example") UserViewExample example);

    int updateByExample(@Param("record") UserView record, @Param("example") UserViewExample example);

    int updateByPrimaryKeySelective(UserView record);

    int updateByPrimaryKey(UserView record);

    int batchInsert(@Param("list") List<UserView> list);

    int batchInsertSelective(@Param("list") List<UserView> list, @Param("selective") UserView.Column ... selective);
}