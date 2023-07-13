package io.metersphere.plan.service.remote.api;

import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SubListUtil;
import io.metersphere.dto.*;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.request.api.ApiTestCaseRequest;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.utils.TestPlanReportUtil;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.utils.BatchProcessingUtil;
import io.metersphere.utils.DiscoveryUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
    public void calculatePlanReport(String planId, TestPlanReportDataStruct report) {
        if (DiscoveryUtil.hasService(MicroServiceName.API_TEST)) {
            List<PlanReportCaseDTO> planReportCaseDTOS = selectStatusForPlanReport(planId);
            //计算测试计划中接口用例的相关数据
            calculatePlanReport(report, planReportCaseDTOS);
            //记录接口用例的运行环境信息
            List<String> idList = planReportCaseDTOS.stream().map(PlanReportCaseDTO::getId).collect(Collectors.toList());
            AutomationsRunInfoDTO automationsRunInfoDTO = getPlanProjectEnvMap(idList);
            if (automationsRunInfoDTO != null) {
                report.setProjectEnvMap(TestPlanReportUtil.mergeProjectEnvMap(report.getProjectEnvMap(), automationsRunInfoDTO.getProjectEnvMap()));
                report.setResourcePools(TestPlanReportUtil.mergeResourcePools(report.getResourcePools(), automationsRunInfoDTO.getResourcePools()));
            }
        }
    }


    public void calculateReportByApiCase(List<TestPlanApiDTO> testPlanApiDTOList, TestPlanReportDataStruct report) {
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

    public void calculatePlanReport(List<String> apiReportIds, TestPlanReportDataStruct report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOS = planApiDefinitionExecResultService.selectForPlanReport(apiReportIds);
            calculatePlanReport(report, planReportCaseDTOS);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }


    public void calculatePlanReport(TestPlanReportDataStruct report, List<PlanReportCaseDTO> planReportCaseDTOS) {
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

    public AutomationsRunInfoDTO getPlanProjectEnvMap(List<String> resourceIds) {
        return microService.postForData(serviceName, BASE_UEL + "/get/plan/env/map", resourceIds, AutomationsRunInfoDTO.class);
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
        //分批处理参数时为了不影响初始参数，这里使用新的对象进行处理
        List<TestPlanApiDTO> returnList = new ArrayList<>();
        SubListUtil.dealForSubList(apiAllCases, 200, (list) -> {
            returnList.addAll(microService.postForDataArray(serviceName, BASE_UEL + "/build/response", list, TestPlanApiDTO.class));
        });
        return returnList;
    }

    public Object relevanceList(int pageNum, int pageSize, ApiTestCaseRequest request) {
        request.setAllowedRepeatCase(testPlanService.isAllowedRepeatCase(request.getPlanId()));
        return microService.postForData(serviceName, BASE_UEL + String.format("/relevance/list/%s/%s", pageNum, pageSize), request);
    }

    public List<ApiDefinitionExecResultWithBLOBs> selectExtForPlanReport(String planId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/get/report/ext/" + planId, ApiDefinitionExecResultWithBLOBs.class);
    }

}
