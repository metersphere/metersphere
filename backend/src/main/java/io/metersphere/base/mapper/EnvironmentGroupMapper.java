package io.metersphere.base.mapper;

import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.base.domain.EnvironmentGroupExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnvironmentGroupMapper {
    long countByExample(EnvironmentGroupExample example);

    int deleteByExample(EnvironmentGroupExample example);

    int deleteByPrimaryKey(String id);

    int insert(EnvironmentGroup record);

    int insertSelective(EnvironmentGroup record);

    List<EnvironmentGroup> selectByExample(EnvironmentGroupExample example);

    EnvironmentGroup selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") EnvironmentGroup record, @Param("example") EnvironmentGroupExample example);

    int updateByExample(@Param("record") EnvironmentGroup record, @Param("example") EnvironmentGroupExample example);

    int updateByPrimaryKeySelective(EnvironmentGroup record);

    int updateByPrimaryKey(EnvironmentGroup record);
}