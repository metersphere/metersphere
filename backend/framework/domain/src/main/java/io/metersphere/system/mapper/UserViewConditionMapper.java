package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserViewCondition;
import io.metersphere.system.domain.UserViewConditionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserViewConditionMapper {
    long countByExample(UserViewConditionExample example);

    int deleteByExample(UserViewConditionExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserViewCondition record);

    int insertSelective(UserViewCondition record);

    List<UserViewCondition> selectByExample(UserViewConditionExample example);

    UserViewCondition selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserViewCondition record, @Param("example") UserViewConditionExample example);

    int updateByExample(@Param("record") UserViewCondition record, @Param("example") UserViewConditionExample example);

    int updateByPrimaryKeySelective(UserViewCondition record);

    int updateByPrimaryKey(UserViewCondition record);

    int batchInsert(@Param("list") List<UserViewCondition> list);

    int batchInsertSelective(@Param("list") List<UserViewCondition> list, @Param("selective") UserViewCondition.Column ... selective);
}