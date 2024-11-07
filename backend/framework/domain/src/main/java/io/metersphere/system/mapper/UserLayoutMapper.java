package io.metersphere.system.mapper;

import io.metersphere.system.domain.UserLayout;
import io.metersphere.system.domain.UserLayoutExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserLayoutMapper {
    long countByExample(UserLayoutExample example);

    int deleteByExample(UserLayoutExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserLayout record);

    int insertSelective(UserLayout record);

    List<UserLayout> selectByExampleWithBLOBs(UserLayoutExample example);

    List<UserLayout> selectByExample(UserLayoutExample example);

    UserLayout selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserLayout record, @Param("example") UserLayoutExample example);

    int updateByExampleWithBLOBs(@Param("record") UserLayout record, @Param("example") UserLayoutExample example);

    int updateByExample(@Param("record") UserLayout record, @Param("example") UserLayoutExample example);

    int updateByPrimaryKeySelective(UserLayout record);

    int updateByPrimaryKeyWithBLOBs(UserLayout record);

    int updateByPrimaryKey(UserLayout record);

    int batchInsert(@Param("list") List<UserLayout> list);

    int batchInsertSelective(@Param("list") List<UserLayout> list, @Param("selective") UserLayout.Column ... selective);
}