package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ExtApiScenarioReportProvider {
    public String insertListSql(List<ApiScenarioReportResult> list) {
        StringBuffer sqlList = new StringBuffer();
        sqlList.append("INSERT INTO api_scenario_report (id, project_id, `name`, create_time, update_time, `status`, user_id, trigger_mode," +
                " execute_type, scenario_name, scenario_id, create_user, actuator, end_time, report_version, version_id, description,report_type,env_config,relevance_test_plan_report_id) VALUES ");
        for (int i = 0; i < list.size(); i++) {
            ApiScenarioReportResult result = list.get(i);
            sqlList.append(" (")
                    .append("'")
                    .append(result.getId())
                    .append("','")
                    .append(result.getProjectId())
                    .append("','")
                    .append(translate(result.getName()))
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
                    .append(translate(result.getScenarioName()))
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
                    .append("','")
                    .append(result.getEnvConfig());
            //判断有没有关联的测试报告ID. 要注意如果赋null的话前后不能有 ' '，否则会变成字符串'null'
            if (StringUtils.isBlank(result.getRelevanceTestPlanReportId())) {
                sqlList.append("', null");
            } else {
                sqlList.append("','")
                        .append(result.getRelevanceTestPlanReportId())
                        .append("'");
            }
            sqlList.append(")");
            if (i < list.size() - 1) {
                sqlList.append(",");
            }
        }
        return sqlList.toString();
    }

    private String translate(String name) {
        if (StringUtils.isNotBlank(name)) {
            name = StringUtils.replace(name, "\'", "\\'");
            name = StringUtils.replace(name, "${", "$ {");
        }
        return name;
    }
}
