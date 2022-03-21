package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.APIScenarioReportResult;

import java.util.List;

public class ExtApiScenarioReportProvider {
    public String insertListSql(List<APIScenarioReportResult> list) {
        StringBuffer sqlList = new StringBuffer();
        sqlList.append("INSERT INTO api_scenario_report (id, project_id, `name`, create_time, update_time, `status`, user_id, trigger_mode," +
                " execute_type, scenario_name, scenario_id, create_user, actuator, end_time, report_version, version_id, description,report_type) VALUES ");
        for (int i = 0; i < list.size(); i++) {
            APIScenarioReportResult result = list.get(i);
            sqlList.append(" (")
                    .append("'")
                    .append(result.getId())
                    .append("','")
                    .append(result.getProjectId())
                    .append("','")
                    .append(result.getName().replace("\'", "\\'"))
                    .append("',")
                    .append(result.getCreateTime())
                    .append(",")
                    .append(result.getUpdateTime())
                    .append(",'")
                    .append(result.getStatus())
                    .append("','")
                    .append(result.getUserId())
                    .append("','")
                    .append(result.getTriggerMode())
                    .append("','")
                    .append(result.getExecuteType())
                    .append("','")
                    .append(result.getScenarioName().replace("\'", "\\'"))
                    .append("','")
                    .append(result.getScenarioId())
                    .append("','")
                    .append(result.getCreateUser())
                    .append("','")
                    .append(result.getActuator())
                    .append("',")
                    .append(result.getEndTime())
                    .append(",")
                    .append(2)
                    .append(",'")
                    .append(result.getVersionId())
                    .append("','")
                    .append(result.getDescription())
                    .append("','")
                    .append(result.getReportType())
                    .append("'")
                    .append(")");
            if (i < list.size() - 1) {
                sqlList.append(",");
            }
        }
        return sqlList.toString();
    }
}
