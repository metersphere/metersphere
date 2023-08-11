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

    List<Environment> selectByExample(EnvironmentExample example);

    Environment selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Environment record, @Param("example") EnvironmentExample example);

    int updateByExample(@Param("record") Environment record, @Param("example") EnvironmentExample example);

    int updateByPrimaryKeySelective(Environment record);

    int updateByPrimaryKey(Environment record);

    int batchInsert(@Param("list") List<Environment> list);

    int batchInsertSelective(@Param("list") List<Environment> list, @Param("selective") Environment.Column ... selective);
}