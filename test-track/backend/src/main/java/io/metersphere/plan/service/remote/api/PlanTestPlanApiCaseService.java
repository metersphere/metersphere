package io.metersphere.plan.service.remote.api;

import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiScenarioReportWithBLOBs;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.plan.dto.ApiModuleDTO;
import io.metersphere.plan.dto.BatchRunDefinitionRequest;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.plan.request.api.ApiTestCaseRequest;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.utils.DiscoveryUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanTestPlanApiCaseService extends ApiTestService {

    private static final String BASE_UEL = "/test/plan/api/case";

    @Resource
    PlanApiDefinitionExecResultService planApiDefinitionExecResultService;
    @Resource
    @Lazy
    TestPlanService testPlanService;

    /**
     * 计算测试计划中接口用例的相关数据
     *
     * @param planId
     * @param report
     * @return 接口用例的最新执行报告
     */
    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        if (DiscoveryUtil.hasService(MicroServiceName.API_TEST)) {
            List<PlanReportCaseDTO> planReportCaseDTOS = selectStatusForPlanReport(planId);
            //计算测试计划中接口用例的相关数据
            calculatePlanReport(report, planReportCaseDTOS);
            //记录接口用例的运行环境信息
            List<String> idList = planReportCaseDTOS.stream().map(PlanReportCaseDTO::getId).collect(Collectors.toList());
            try {
                if (MapUtils.isEmpty(report.getProjectEnvMap())) {
                    report.setProjectEnvMap(getPlanProjectEnvMap(idList));
                } else {
                    Map<String, List<String>> projectEnvMap = getPlanProjectEnvMap(idList);
                    if (MapUtils.isNotEmpty(projectEnvMap)) {
                        for (Map.Entry<String, List<String>> entry : projectEnvMap.entrySet()) {
                            String project = entry.getKey();
                            List<String> envList = entry.getValue();
                            if (report.getProjectEnvMap().containsKey(project)) {
                                for (String env : envList) {
                                    if (!report.getProjectEnvMap().get(project).contains(env)) {
                                        report.getProjectEnvMap().get(project).add(env);
                                    }
                                }
                            } else {
                                report.getProjectEnvMap().put(project, envList);
                            }

                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
    }


    public void calculateReportByApiCase(List<TestPlanApiDTO> testPlanApiDTOList, TestPlanSimpleReportDTO report) {
        try {
            if (CollectionUtils.isNotEmpty(testPlanApiDTOList)) {
                List<PlanReportCaseDTO> planReportCaseDTOList = new ArrayList<>();
                testPlanApiDTOList.forEach(item -> {
                    PlanReportCaseDTO dto = new PlanReportCaseDTO();
                    dto.setId(item.getId());
                    dto.setStatus(item.getExecResult());
                    dto.setReportId(item.getReportId());
                    dto.setCaseId(item.getCaseId());
                    planReportCaseDTOList.add(dto);
                });
                calculatePlanReport(report, planReportCaseDTOList);
            }
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void calculatePlanReport(List<String> apiReportIds, TestPlanSimpleReportDTO report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOS = planApiDefinitionExecResultService.selectForPlanReport(apiReportIds);
            calculatePlanReport(report, planReportCaseDTOS);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }


    public void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanApiResultReportDTO apiResult = report.getApiResult();
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, "success");

        TestPlanStatusCalculator.addToReportCommonStatusResultList(statusResultMap, statusResult);

        apiResult.setApiCaseData(statusResult);
    }

    public List<String> getExecResultByPlanId(String planId) {
        return (List<String>) microService.getForData(serviceName, BASE_UEL + "/plan/exec/result/" + planId);
    }

    public List<TestPlanApiCaseDTO> listByPlanId(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/list/" + planId, TestPlanApiCaseDTO.class);
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/get/report/status/" + planId, PlanReportCaseDTO.class);
    }

    public Map<String, List<String>> getPlanProjectEnvMap(List<String> resourceIds) {
        return (Map<String, List<String>>) microService.postForData(serviceName, BASE_UEL + "/get/plan/env/map", resourceIds);
    }

    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        try {
            return microService.postForDataArray(serviceName, BASE_UEL + "/run", request, MsExecResponseDTO.class);
        } catch (Exception e) {
            LogUtil.info("调用API服务执行用例失败", e);
            return new ArrayList<>();
        }
    }

    public RunModeConfigDTO setApiCaseEnv(String planId, RunModeConfigDTO runModeConfig) {
        return microService.postForData(serviceName, BASE_UEL + "/set/env/" + planId, runModeConfig, RunModeConfigDTO.class);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void relevanceByTestIds(List<String> apiCaseIds, String planId) {
        if (CollectionUtils.isNotEmpty(apiCaseIds)) {
            microService.postForData(serviceName, BASE_UEL + "/relevance/" + planId, apiCaseIds);
        }
    }

    public List<String> getStatusByTestPlanId(String planId) {
        return (List<String>) microService.getForData(serviceName, BASE_UEL + "/status/" + planId);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        microService.getForData(serviceName, BASE_UEL + "/plan/copy/" + sourcePlanId + "/" + targetPlanId);
    }

    public boolean haveExecCase(String planId) {
        try {
            return (boolean) microService.getForData(serviceName, BASE_UEL + "/have/exec/" + planId);
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
    }

    public Map<String, List<String>> getApiCaseEnv(List<String> planApiCaseIds) {
        return (Map<String, List<String>>) microService.postForData(serviceName, BASE_UEL + "/get/env", planApiCaseIds);
    }

    public Map<String, List<String>> getApiCaseEnv(String planId) {
        return (Map<String, List<String>>) microService.getForData(serviceName, BASE_UEL + "/get/env/" + planId);
    }

    public List<String> getApiCaseProjectIds(String planId) {
        return (List<String>) microService.getForData(serviceName, BASE_UEL + "/get/project/ids/" + planId);
    }

    public Boolean isCaseExecuting(String planId) {
        return (Boolean) microService.getForData(serviceName, BASE_UEL + "/is/executing/" + planId);
    }

    public List<TestPlanApiDTO> getFailureListByIds(Set<String> ids) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/failure/list", ids, TestPlanApiDTO.class);
    }

    public Boolean hasFailCase(String planId, List<String> apiCaseIds) {
        return (Boolean) microService.postForData(serviceName, BASE_UEL + "/has/fail/" + planId, apiCaseIds);
    }

    public List<ApiModuleDTO> getNodeByPlanId(List<String> projectIds, String planId, String protocol) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/list/module/" + planId + "/" + protocol, projectIds, ApiModuleDTO.class);
    }

    public List<TestPlanApiDTO> buildResponse(List<TestPlanApiDTO> apiAllCases) {
        if (CollectionUtils.isEmpty(apiAllCases)) {
            return null;
        }
        return microService.postForDataArray(serviceName, BASE_UEL + "/build/response", apiAllCases, TestPlanApiDTO.class);
    }

    public Object relevanceList(int pageNum, int pageSize, ApiTestCaseRequest request) {
        request.setAllowedRepeatCase(testPlanService.isAllowedRepeatCase(request.getPlanId()));
        return microService.postForData(serviceName, BASE_UEL + String.format("/relevance/list/%s/%s", pageNum, pageSize), request);
    }

    public List<ApiDefinitionExecResultWithBLOBs> selectExtForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/get/report/ext/" + planId, ApiDefinitionExecResultWithBLOBs.class);
    }

    public List<ApiScenarioReportWithBLOBs> selectExtForPlanScenarioReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/get/report/scenario/ext/" + planId, ApiScenarioReportWithBLOBs.class);
    }
}
