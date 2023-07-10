package io.metersphere.sdk.mapper;

import io.metersphere.sdk.domain.OperationLogBlob;
import io.metersphere.sdk.domain.OperationLogBlobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OperationLogBlobMapper {
    long countByExample(OperationLogBlobExample example);

    int deleteByExample(OperationLogBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(OperationLogBlob record);

    int insertSelective(OperationLogBlob record);

    List<OperationLogBlob> selectByExampleWithBLOBs(OperationLogBlobExample example);

    List<OperationLogBlob> selectByExample(OperationLogBlobExample example);

    OperationLogBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") OperationLogBlob record, @Param("example") OperationLogBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") OperationLogBlob record, @Param("example") OperationLogBlobExample example);

    int updateByExample(@Param("record") OperationLogBlob record, @Param("example") OperationLogBlobExample example);

    int updateByPrimaryKeySelective(OperationLogBlob record);

    int updateByPrimaryKeyWithBLOBs(OperationLogBlob record);
}