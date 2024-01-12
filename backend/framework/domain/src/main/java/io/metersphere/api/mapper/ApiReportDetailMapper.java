package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReportDetail;
import io.metersphere.api.domain.ApiReportDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiReportDetailMapper {
    long countByExample(ApiReportDetailExample example);

    int deleteByExample(ApiReportDetailExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiReportDetail record);

    int insertSelective(ApiReportDetail record);

    List<ApiReportDetail> selectByExampleWithBLOBs(ApiReportDetailExample example);

    List<ApiReportDetail> selectByExample(ApiReportDetailExample example);

    ApiReportDetail selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiReportDetail record, @Param("example") ApiReportDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiReportDetail record, @Param("example") ApiReportDetailExample example);

    int updateByExample(@Param("record") ApiReportDetail record, @Param("example") ApiReportDetailExample example);

    int updateByPrimaryKeySelective(ApiReportDetail record);

    int updateByPrimaryKeyWithBLOBs(ApiReportDetail record);

    int updateByPrimaryKey(ApiReportDetail record);

    int batchInsert(@Param("list") List<ApiReportDetail> list);

    int batchInsertSelective(@Param("list") List<ApiReportDetail> list, @Param("selective") ApiReportDetail.Column ... selective);
}