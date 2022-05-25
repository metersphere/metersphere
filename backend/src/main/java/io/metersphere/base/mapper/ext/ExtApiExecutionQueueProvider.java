package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiExecutionQueueDetail;

import java.util.List;

public class ExtApiExecutionQueueProvider {
    public String insertListSql(List<ApiExecutionQueueDetail> list) {
        StringBuffer sqlList = new StringBuffer();
        sqlList.append("insert into api_execution_queue_detail (id, queue_id, sort, report_id, test_id, `type`, create_time, evn_map) values ");
        for (int i = 0; i < list.size(); i++) {
            ApiExecutionQueueDetail result = list.get(i);
            sqlList.append(" (")
                    .append("'")
                    .append(result.getId())
                    .append("','")
                    .append(result.getQueueId())
                    .append("',")
                    .append(result.getSort())
                    .append(",'")
                    .append(result.getReportId())
                    .append("','")
                    .append(result.getTestId())
                    .append("','")
                    .append(result.getType())
                    .append("',")
                    .append(result.getCreateTime())
                    .append(",'")
                    .append(result.getEvnMap())
                    .append("'")
                    .append(")");
            if (i < list.size() - 1) {
                sqlList.append(",");
            }
        }
        return sqlList.toString();
    }
}
