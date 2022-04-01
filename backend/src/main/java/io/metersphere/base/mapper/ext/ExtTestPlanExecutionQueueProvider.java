package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanExecutionQueue;

import java.util.List;

public class ExtTestPlanExecutionQueueProvider {
    public String insertListSql(List<TestPlanExecutionQueue> list) {
        StringBuffer sqlList = new StringBuffer();
        sqlList.append("insert into test_plan_execution_queue (id,report_id, run_mode, create_time) values ");
        for (int i = 0; i < list.size(); i++) {
            TestPlanExecutionQueue result = list.get(i);
            sqlList.append(" (")
                    .append("'")
                    .append(result.getId())
                    .append("','")
                    .append(result.getReportId())
                    .append("','")
                    .append(result.getRunMode())
                    .append("','")
                    .append(result.getCreateTime())
                    .append("'")
                    .append(")");
            if (i < list.size() - 1) {
                sqlList.append(",");
            }
        }
        return sqlList.toString();
    }
}
