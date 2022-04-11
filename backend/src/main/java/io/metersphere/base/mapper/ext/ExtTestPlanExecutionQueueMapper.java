package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanExecutionQueue;
import org.apache.ibatis.annotations.InsertProvider;

import java.util.List;

public interface ExtTestPlanExecutionQueueMapper {

    @InsertProvider(type = ExtTestPlanExecutionQueueProvider.class, method = "insertListSql")
    void sqlInsert(List<TestPlanExecutionQueue> list);
}
