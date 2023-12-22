package io.metersphere.service;


import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiScenarioReportWithBLOBs;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiCheckPermissionService {

    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    public void checkReportOwner(String reportId, String permissionId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        ApiScenarioReportWithBLOBs scenarioReport = apiScenarioReportMapper.selectByPrimaryKey(reportId);
        ApiDefinitionExecResultWithBLOBs apiReport = apiDefinitionExecResultMapper.selectByPrimaryKey(reportId);
        if (scenarioReport != null && (!projectIds.contains(scenarioReport.getProjectId()) ||
                !SessionUtils.hasPermission(null, scenarioReport.getProjectId(), permissionId))) {
            MSException.throwException(Translator.get("check_owner_report"));
        }
        if (apiReport != null && (!projectIds.contains(apiReport.getProjectId()) ||
                !SessionUtils.hasPermission(null, apiReport.getProjectId(), permissionId))) {
            MSException.throwException(Translator.get("check_owner_report"));
        }
    }

}
