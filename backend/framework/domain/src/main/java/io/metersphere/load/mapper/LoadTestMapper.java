package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTest;
import io.metersphere.load.domain.LoadTestExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestMapper {
    long countByExample(LoadTestExample example);

    int deleteByExample(LoadTestExample example);

    int deleteByPrimaryKey(String id);

    int insert(LoadTest record);

    int insertSelective(LoadTest record);

    List<LoadTest> selectByExample(LoadTestExample example);

    LoadTest selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LoadTest record, @Param("example") LoadTestExample example);

    int updateByExample(@Param("record") LoadTest record, @Param("example") LoadTestExample example);

    int updateByPrimaryKeySelective(LoadTest record);

    int updateByPrimaryKey(LoadTest record);
}