package io.metersphere.load.mapper;

import io.metersphere.load.domain.LoadTestBlob;
import io.metersphere.load.domain.LoadTestBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LoadTestBlobMapper {
    long countByExample(LoadTestBlobExample example);

    int deleteByExample(LoadTestBlobExample example);

    int deleteByPrimaryKey(String testId);

    int insert(LoadTestBlob record);

    int insertSelective(LoadTestBlob record);

    List<LoadTestBlob> selectByExampleWithBLOBs(LoadTestBlobExample example);

    List<LoadTestBlob> selectByExample(LoadTestBlobExample example);

    LoadTestBlob selectByPrimaryKey(String testId);

    int updateByExampleSelective(@Param("record") LoadTestBlob record, @Param("example") LoadTestBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") LoadTestBlob record, @Param("example") LoadTestBlobExample example);

    int updateByExample(@Param("record") LoadTestBlob record, @Param("example") LoadTestBlobExample example);

    int updateByPrimaryKeySelective(LoadTestBlob record);

    int updateByPrimaryKeyWithBLOBs(LoadTestBlob record);
}