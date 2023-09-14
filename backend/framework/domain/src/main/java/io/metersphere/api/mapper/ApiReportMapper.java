package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiReportMapper {
    long countByExample(ApiReportExample example);

    int deleteByExample(ApiReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiReport record);

    int insertSelective(ApiReport record);

    List<ApiReport> selectByExample(ApiReportExample example);

    ApiReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiReport record, @Param("example") ApiReportExample example);

    int updateByExample(@Param("record") ApiReport record, @Param("example") ApiReportExample example);

    int updateByPrimaryKeySelective(ApiReport record);

    int updateByPrimaryKey(ApiReport record);

    int batchInsert(@Param("list") List<ApiReport> list);

    int batchInsertSelective(@Param("list") List<ApiReport> list, @Param("selective") ApiReport.Column ... selective);
}