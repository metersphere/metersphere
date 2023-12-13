package io.metersphere.service.definition;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.service.BaseCheckPermissionService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiCheckPermissionService {

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;

    public void checkApiCaseOwner(String caseId, String permissionId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        ApiTestCaseWithBLOBs apiTestCase = apiTestCaseMapper.selectByPrimaryKey(caseId);
        if (apiTestCase == null) {
            MSException.throwException(Translator.get("check_owner_case"));
        }
        if (!projectIds.contains(apiTestCase.getProjectId())) {
            MSException.throwException(Translator.get("check_owner_case"));
        }
        if (!SessionUtils.hasPermission(null,apiTestCase.getProjectId(), permissionId)) {
            MSException.throwException(Translator.get("check_owner_case"));
        }
    }

    public void checkApiOwner(String apiId, String permissionId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        ApiDefinitionWithBLOBs apiInfo = apiDefinitionMapper.selectByPrimaryKey(apiId);
        if (apiInfo == null) {
            MSException.throwException(Translator.get("check_owner_api"));
        }
        if (!projectIds.contains(apiInfo.getProjectId())) {
            MSException.throwException(Translator.get("check_owner_api"));
        }
        if (!SessionUtils.hasPermission(null,apiInfo.getProjectId(), permissionId)) {
            MSException.throwException(Translator.get("check_owner_api"));
        }
    }

    public void checkApiScenarioOwner(String scenarioId, String permissionId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(scenarioId);
        if (scenario == null) {
            MSException.throwException(Translator.get("check_owner_scenario"));
        }
        if (!projectIds.contains(scenario.getProjectId())) {
            MSException.throwException(Translator.get("check_owner_scenario"));
        }
        if (!SessionUtils.hasPermission(null,scenario.getProjectId(), permissionId)) {
            MSException.throwException(Translator.get("check_owner_scenario"));
        }
    }

    public void checkApiScenarioReportOwner(String reportId, String permissionId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        ApiScenarioReportWithBLOBs apiScenario = apiScenarioReportMapper.selectByPrimaryKey(reportId);
        ApiDefinitionExecResultWithBLOBs apiReport = apiDefinitionExecResultMapper.selectByPrimaryKey(reportId);
        if (apiScenario == null) {
            if (apiReport == null) {
                MSException.throwException(Translator.get("check_owner_report"));
            }
            MSException.throwException(Translator.get("check_owner_report"));
        }
        if (apiScenario != null) {
            if (!projectIds.contains(apiScenario.getProjectId())) {
                MSException.throwException(Translator.get("check_owner_report"));
            }
            if (!SessionUtils.hasPermission(null,apiScenario.getProjectId(), permissionId)) {
                MSException.throwException(Translator.get("check_owner_report"));
            }
        }
        if (apiReport != null) {
            if (!projectIds.contains(apiReport.getProjectId())) {
                MSException.throwException(Translator.get("check_owner_report"));
            }
            if (!SessionUtils.hasPermission(null,apiReport.getProjectId(), permissionId)) {
                MSException.throwException(Translator.get("check_owner_report"));
            }
        }

    }
}
