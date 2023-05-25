package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestApi;
import io.metersphere.load.domain.LoadTestApiExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestApiMapper {
    long countByExample(LoadTestApiExample example);

    int deleteByExample(LoadTestApiExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTestApi record);

    int insertSelective(LoadTestApi record);

    List<LoadTestApi> selectByExample(LoadTestApiExample example);

    LoadTestApi selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTestApi record, @Param("example") LoadTestApiExample example);

    int updateByExample(@Param("record") LoadTestApi record, @Param("example") LoadTestApiExample example);

    int updateByPrimaryKeySelective(LoadTestApi record);

    int updateByPrimaryKey(LoadTestApi record);
}