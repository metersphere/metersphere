package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserKey;
import io.metersphere.system.domain.UserKeyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserKeyMapper {
    long countByExample(UserKeyExample example);

    int deleteByExample(UserKeyExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserKey record);

    int insertSelective(UserKey record);

    List<UserKey> selectByExample(UserKeyExample example);

    UserKey selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserKey record, @Param("example") UserKeyExample example);

    int updateByExample(@Param("record") UserKey record, @Param("example") UserKeyExample example);

    int updateByPrimaryKeySelective(UserKey record);

    int updateByPrimaryKey(UserKey record);

    int batchInsert(@Param("list") List<UserKey> list);

    int batchInsertSelective(@Param("list") List<UserKey> list, @Param("selective") UserKey.Column ... selective);
}