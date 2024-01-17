package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReportLog;
import io.metersphere.api.domain.ApiReportLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiReportLogMapper {
    long countByExample(ApiReportLogExample example);

    int deleteByExample(ApiReportLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiReportLog record);

    int insertSelective(ApiReportLog record);

    List<ApiReportLog> selectByExampleWithBLOBs(ApiReportLogExample example);

    List<ApiReportLog> selectByExample(ApiReportLogExample example);

    ApiReportLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiReportLog record, @Param("example") ApiReportLogExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiReportLog record, @Param("example") ApiReportLogExample example);

    int updateByExample(@Param("record") ApiReportLog record, @Param("example") ApiReportLogExample example);

    int updateByPrimaryKeySelective(ApiReportLog record);

    int updateByPrimaryKeyWithBLOBs(ApiReportLog record);

    int updateByPrimaryKey(ApiReportLog record);

    int batchInsert(@Param("list") List<ApiReportLog> list);

    int batchInsertSelective(@Param("list") List<ApiReportLog> list, @Param("selective") ApiReportLog.Column ... selective);
}