package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiDebugBlob;
import io.metersphere.api.domain.ApiDebugBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiDebugBlobMapper {
    long countByExample(ApiDebugBlobExample example);

    int deleteByExample(ApiDebugBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDebugBlob record);

    int insertSelective(ApiDebugBlob record);

    List<ApiDebugBlob> selectByExampleWithBLOBs(ApiDebugBlobExample example);

    List<ApiDebugBlob> selectByExample(ApiDebugBlobExample example);

    ApiDebugBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDebugBlob record, @Param("example") ApiDebugBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDebugBlob record, @Param("example") ApiDebugBlobExample example);

    int updateByExample(@Param("record") ApiDebugBlob record, @Param("example") ApiDebugBlobExample example);

    int updateByPrimaryKeySelective(ApiDebugBlob record);

    int updateByPrimaryKeyWithBLOBs(ApiDebugBlob record);

    int batchInsert(@Param("list") List<ApiDebugBlob> list);

    int batchInsertSelective(@Param("list") List<ApiDebugBlob> list, @Param("selective") ApiDebugBlob.Column ... selective);
}