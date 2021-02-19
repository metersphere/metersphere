package io.metersphere.base.mapper;

import io.metersphere.base.domain.UserHeader;
import io.metersphere.base.domain.UserHeaderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserHeaderMapper {
    long countByExample(UserHeaderExample example);

    int deleteByExample(UserHeaderExample example);

    int deleteByPrimaryKey(String id);

    int insert(UserHeader record);

    int insertSelective(UserHeader record);

    List<UserHeader> selectByExample(UserHeaderExample example);

    UserHeader selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") UserHeader record, @Param("example") UserHeaderExample example);

    int updateByExample(@Param("record") UserHeader record, @Param("example") UserHeaderExample example);

    int updateByPrimaryKeySelective(UserHeader record);

    int updateByPrimaryKey(UserHeader record);
}