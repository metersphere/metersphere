package io.metersphere.base.mapper;

import io.metersphere.base.domain.LoadTestFile;
import io.metersphere.base.domain.LoadTestFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoadTestFileMapper {
    long countByExample(LoadTestFileExample example);

    int deleteByExample(LoadTestFileExample example);

    int insert(LoadTestFile record);

    int insertSelective(LoadTestFile record);

    List<LoadTestFile> selectByExample(LoadTestFileExample example);

    int updateByExampleSelective(@Param("record") LoadTestFile record, @Param("example") LoadTestFileExample example);

    int updateByExample(@Param("record") LoadTestFile record, @Param("example") LoadTestFileExample example);
}