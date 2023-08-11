package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnvironmentBlobMapper {
    long countByExample(EnvironmentBlobExample example);

    int deleteByExample(EnvironmentBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(EnvironmentBlob record);

    int insertSelective(EnvironmentBlob record);

    List<EnvironmentBlob> selectByExampleWithBLOBs(EnvironmentBlobExample example);

    List<EnvironmentBlob> selectByExample(EnvironmentBlobExample example);

    EnvironmentBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") EnvironmentBlob record, @Param("example") EnvironmentBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") EnvironmentBlob record, @Param("example") EnvironmentBlobExample example);

    int updateByExample(@Param("record") EnvironmentBlob record, @Param("example") EnvironmentBlobExample example);

    int updateByPrimaryKeySelective(EnvironmentBlob record);

    int updateByPrimaryKeyWithBLOBs(EnvironmentBlob record);

    int batchInsert(@Param("list") List<EnvironmentBlob> list);

    int batchInsertSelective(@Param("list") List<EnvironmentBlob> list, @Param("selective") EnvironmentBlob.Column ... selective);
}