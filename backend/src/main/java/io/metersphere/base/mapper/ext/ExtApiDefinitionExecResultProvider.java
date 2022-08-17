package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ExtApiDefinitionExecResultProvider {
    public String insertListSql(List<ApiDefinitionExecResultWithBLOBs> list) {
        StringBuffer sqlList = new StringBuffer();
        sqlList.append("insert into api_definition_exec_result (id, `name`, resource_id, `status`, user_id, start_time, end_time," +
                " create_time, `type`, actuator, trigger_mode, version_id, error_code,project_id,integrated_report_id,report_type, content,env_config,relevance_test_plan_report_id) values ");
        for (int i = 0; i < list.size(); i++) {
            ApiDefinitionExecResultWithBLOBs result = list.get(i);
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
                    .append("','")
                    .append(result.getEnvConfig());
            //判断有没有关联的测试报告ID
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
}
