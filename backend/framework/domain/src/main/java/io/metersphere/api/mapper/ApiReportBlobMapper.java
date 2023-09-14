package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReportBlob;
import io.metersphere.api.domain.ApiReportBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiReportBlobMapper {
    long countByExample(ApiReportBlobExample example);

    int deleteByExample(ApiReportBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiReportBlob record);

    int insertSelective(ApiReportBlob record);

    List<ApiReportBlob> selectByExampleWithBLOBs(ApiReportBlobExample example);

    List<ApiReportBlob> selectByExample(ApiReportBlobExample example);

    ApiReportBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiReportBlob record, @Param("example") ApiReportBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiReportBlob record, @Param("example") ApiReportBlobExample example);

    int updateByExample(@Param("record") ApiReportBlob record, @Param("example") ApiReportBlobExample example);

    int updateByPrimaryKeySelective(ApiReportBlob record);

    int updateByPrimaryKeyWithBLOBs(ApiReportBlob record);

    int batchInsert(@Param("list") List<ApiReportBlob> list);

    int batchInsertSelective(@Param("list") List<ApiReportBlob> list, @Param("selective") ApiReportBlob.Column ... selective);
}