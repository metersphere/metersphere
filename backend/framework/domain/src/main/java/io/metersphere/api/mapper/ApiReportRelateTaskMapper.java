package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiReportRelateTask;
import io.metersphere.api.domain.ApiReportRelateTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiReportRelateTaskMapper {
    long countByExample(ApiReportRelateTaskExample example);

    int deleteByExample(ApiReportRelateTaskExample example);

    int deleteByPrimaryKey(@Param("taskResourceId") String taskResourceId, @Param("reportId") String reportId);

    int insert(ApiReportRelateTask record);

    int insertSelective(ApiReportRelateTask record);

    List<ApiReportRelateTask> selectByExample(ApiReportRelateTaskExample example);

    int updateByExampleSelective(@Param("record") ApiReportRelateTask record, @Param("example") ApiReportRelateTaskExample example);

    int updateByExample(@Param("record") ApiReportRelateTask record, @Param("example") ApiReportRelateTaskExample example);

    int batchInsert(@Param("list") List<ApiReportRelateTask> list);

    int batchInsertSelective(@Param("list") List<ApiReportRelateTask> list, @Param("selective") ApiReportRelateTask.Column ... selective);
}