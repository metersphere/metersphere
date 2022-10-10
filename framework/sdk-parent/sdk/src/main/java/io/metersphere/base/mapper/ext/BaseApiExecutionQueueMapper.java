package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import org.apache.ibatis.annotations.InsertProvider;

import java.util.List;

public interface BaseApiExecutionQueueMapper {
    void delete();

    List<ApiExecutionQueue> findTestPlanReportQueue();

    List<String> findTestPlanRunningReport();

    @InsertProvider(type = BaseApiExecutionQueueProvider.class, method = "insertListSql")
    void sqlInsert(List<ApiExecutionQueueDetail> list);

}