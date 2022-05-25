package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiDefinitionExecResult;

import java.util.List;

public class ExtApiDefinitionExecResultProvider {
    public String insertListSql(List<ApiDefinitionExecResult> list) {
        StringBuffer sqlList = new StringBuffer();
        sqlList.append("insert into api_definition_exec_result (id, `name`, resource_id, `status`, user_id, start_time, end_time," +
                " create_time, `type`, actuator, trigger_mode, version_id, error_code,project_id,integrated_report_id,report_type, content) values ");
        for (int i = 0; i < list.size(); i++) {
            ApiDefinitionExecResult result = list.get(i);
            sqlList.append(" (")
                    .append("'")
                    .append(result.getId())
                    .append("','")
                    .append(result.getName().replace("\'", "\\'"))
                    .append("','")
                    .append(result.getResourceId())
                    .append("','")
                    .append(result.getStatus())
                    .append("','")
                    .append(result.getUserId())
                    .append("',")
                    .append(result.getStartTime())
                    .append(",")
                    .append(result.getEndTime())
                    .append(",")
                    .append(result.getCreateTime())
                    .append(",'")
                    .append(result.getType())
                    .append("','")
                    .append(result.getActuator())
                    .append("','")
                    .append(result.getTriggerMode())
                    .append("','")
                    .append(result.getVersionId())
                    .append("','")
                    .append(result.getErrorCode())
                    .append("','")
                    .append(result.getProjectId())
                    .append("','")
                    .append(result.getIntegratedReportId())
                    .append("','")
                    .append(result.getReportType())
                    .append("','")
                    .append(result.getContent())
                    .append("'")
                    .append(")");
            if (i < list.size() - 1) {
                sqlList.append(",");
            }
        }
        return sqlList.toString();
    }
}
