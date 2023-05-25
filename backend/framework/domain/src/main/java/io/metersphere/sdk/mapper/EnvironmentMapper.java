package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnvironmentMapper {
    long countByExample(EnvironmentExample example);

    int deleteByExample(EnvironmentExample example);

    int deleteByPrimaryKey(String id);

    int insert(Environment record);

    int insertSelective(Environment record);

    List<Environment> selectByExampleWithBLOBs(EnvironmentExample example);

    List<Environment> selectByExample(EnvironmentExample example);

    Environment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Environment record, @Param("example") EnvironmentExample example);

    int updateByExampleWithBLOBs(@Param("record") Environment record, @Param("example") EnvironmentExample example);

    int updateByExample(@Param("record") Environment record, @Param("example") EnvironmentExample example);

    int updateByPrimaryKeySelective(Environment record);

    int updateByPrimaryKeyWithBLOBs(Environment record);

    int updateByPrimaryKey(Environment record);
}