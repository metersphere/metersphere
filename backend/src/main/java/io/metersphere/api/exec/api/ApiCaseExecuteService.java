package io.metersphere.api.exec.api;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiCaseRunRequest;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.exec.utils.ApiDefinitionExecResultUtil;
import io.metersphere.api.service.ApiCaseResultService;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.ApiScenarioReportStructureService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ApiCaseExecuteService {
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiCaseParallelExecuteService apiCaseParallelExecuteService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiCaseResultService apiCaseResultService;
    @Resource
    ApiScenarioReportStructureService apiScenarioReportStructureService;

    /**
     * 测试计划case执行
     *
     * @param request
     * @return
     */
    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        List<String> ids = request.getPlanIds();
        if (CollectionUtils.isEmpty(ids)) {
            return new LinkedList<>();
        }
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }
        if (StringUtils.equals("GROUP", request.getConfig().getEnvironmentType()) && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }
        LoggerUtil.debug("开始查询测试计划用例");

        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(ids);
        example.setOrderByClause("`order` DESC");
        List<TestPlanApiCase> planApiCases = testPlanApiCaseMapper.selectByExample(example);
        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ApiRunMode.API_PLAN.name());
        }

        Map<String, ApiDefinitionExecResult> executeQueue = new LinkedHashMap<>();
        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        String status = request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString()) ? APITestStatus.Waiting.name() : APITestStatus.Running.name();
        Map<String, String> planProjects = new HashMap<>();
        planApiCases.forEach(testPlanApiCase -> {
            ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.addResult(request, testPlanApiCase, status);
            if (planProjects.containsKey(testPlanApiCase.getTestPlanId())) {
                report.setProjectId(planProjects.get(testPlanApiCase.getTestPlanId()));
            } else {
                TestPlan plan = CommonBeanFactory.getBean(TestPlanMapper.class).selectByPrimaryKey(testPlanApiCase.getTestPlanId());
                if (plan != null) {
                    planProjects.put(plan.getId(), plan.getProjectId());
                    report.setProjectId(plan.getProjectId());
                }
            }
            executeQueue.put(testPlanApiCase.getId(), report);
            responseDTOS.add(new MsExecResponseDTO(testPlanApiCase.getId(), report.getId(), request.getTriggerMode()));
        });

        apiCaseResultService.batchSave(executeQueue);

        LoggerUtil.debug("开始生成测试计划队列");
        String reportType = request.getConfig().getReportType();
        String poolId = request.getConfig().getResourcePoolId();
        String runMode = StringUtils.equals(request.getTriggerMode(), TriggerMode.MANUAL.name()) ? ApiRunMode.API_PLAN.name() : ApiRunMode.SCHEDULE_API_PLAN.name();
        DBTestQueue deQueue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.API_PLAN.name(), request.getPlanReportId(), reportType, runMode, request.getConfig());

        // 开始选择执行模式
        if (deQueue != null && deQueue.getQueue() != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("PLAN-CASE：" + request.getPlanReportId());
                    if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                        apiScenarioSerialService.serial(deQueue, deQueue.getQueue());
                    } else {
                        apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), deQueue, runMode);
                    }
                }
            });
            thread.start();
        }
        return responseDTOS;
    }

    public void checkEnv(List<ApiTestCaseWithBLOBs> caseList) {
        if (CollectionUtils.isNotEmpty(caseList)) {
            StringBuilder builderHttp = new StringBuilder();
            StringBuilder builderTcp = new StringBuilder();
            for (int i = caseList.size() - 1; i >= 0; i--) {
                ApiTestCaseWithBLOBs apiCase = caseList.get(i);
                JSONObject apiCaseNew = new JSONObject(apiCase.getRequest());
                if (apiCaseNew.has("type") && "HTTPSamplerProxy".equals(apiCaseNew.getString("type"))) {
                    if (!apiCaseNew.has("useEnvironment") || StringUtils.isEmpty(apiCaseNew.getString("useEnvironment"))) {
                        builderHttp.append(apiCase.getName()).append("; ");
                    }
                }
                if (apiCaseNew.has("type") && "JDBCSampler".equals(apiCaseNew.getString("type"))) {
                    DatabaseConfig dataSource = null;
                    if (apiCaseNew.has("useEnvironment") && apiCaseNew.has("dataSourceId")) {
                        String environmentId = apiCaseNew.getString("useEnvironment");
                        String dataSourceId = apiCaseNew.getString("dataSourceId");
                        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                        ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
                        EnvironmentConfig envConfig = null;
                        if (environment != null && environment.getConfig() != null) {
                            envConfig = com.alibaba.fastjson.JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                            if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                                for (DatabaseConfig item : envConfig.getDatabaseConfigs()) {
                                    if (item.getId().equals(dataSourceId)) {
                                        dataSource = item;
                                    }
                                }
                            }
                        }
                    }
                    if (dataSource == null) {
                        builderTcp.append(apiCase.getName()).append("; ");
                    }
                }
            }
            if (StringUtils.isNotEmpty(builderHttp)) {
                MSException.throwException("用例：" + builderHttp + "运行环境为空！请检查");
            }
            if (StringUtils.isNotEmpty(builderTcp)) {
                MSException.throwException("用例：" + builderTcp + "数据源为空！请检查");
            }
        }
    }
    /**
     * 接口定义case执行
     *
     * @param request
     * @return
     */
    public List<MsExecResponseDTO> run(ApiCaseRunRequest request) {
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("进入执行方法，接收到参数：" + JSON.toJSONString(request));
        }
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }

        if (StringUtils.equals("GROUP", request.getConfig().getEnvironmentType()) && StringUtils.isNotEmpty(request.getConfig().getEnvironmentGroupId())) {
            request.getConfig().setEnvMap(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiTestCaseMapper.selectIdsByQuery((ApiTestCaseRequest) query));

        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(request.getIds());
        List<ApiTestCaseWithBLOBs> caseList = apiTestCaseMapper.selectByExampleWithBLOBs(example);
        LoggerUtil.debug("查询到执行数据：" + caseList.size());
        // 环境检查
        if (MapUtils.isEmpty(request.getConfig().getEnvMap())) {
            this.checkEnv(caseList);
        }
        // 集合报告设置
        String serialReportId = null;
        if (StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString())
                && StringUtils.isNotEmpty(request.getConfig().getReportName())) {
            serialReportId = UUID.randomUUID().toString();
            ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.initBase(null, APITestStatus.Running.name(), serialReportId, request.getConfig());
            report.setName(request.getConfig().getReportName());
            report.setProjectId(request.getProjectId());
            report.setReportType(ReportTypeConstants.API_INTEGRATED.name());
            report.setVersionId(caseList.get(0).getVersionId());
            Map<String, ApiDefinitionExecResult> executeQueue = new LinkedHashMap<>();
            executeQueue.put(serialReportId, report);

            apiScenarioReportStructureService.save(serialReportId, new ArrayList<>());
            apiCaseResultService.batchSave(executeQueue);
        }

        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            if (request.getCondition() == null || !request.getCondition().isSelectAll()) {
                // 按照id指定顺序排序
                FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(request.getIds());
                fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
                BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
                Collections.sort(caseList, beanComparator);
            }
        }

        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ApiRunMode.DEFINITION.name());
        }

        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        Map<String, ApiDefinitionExecResult> executeQueue = new LinkedHashMap<>();
        String status = request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString()) ? APITestStatus.Waiting.name() : APITestStatus.Running.name();

        for (int i = 0; i < caseList.size(); i++) {
            ApiTestCaseWithBLOBs caseWithBLOBs = caseList.get(i);
            ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.initBase(caseWithBLOBs.getId(), APITestStatus.Running.name(), null, request.getConfig());
            report.setStatus(status);
            report.setName(caseWithBLOBs.getName());
            report.setProjectId(caseWithBLOBs.getProjectId());
            report.setVersionId(caseWithBLOBs.getVersionId());
            report.setCreateTime(System.currentTimeMillis() + i);
            if (StringUtils.isNotEmpty(serialReportId)) {
                report.setIntegratedReportId(serialReportId);
            }
            executeQueue.put(caseWithBLOBs.getId(), report);
            responseDTOS.add(new MsExecResponseDTO(caseWithBLOBs.getId(), report.getId(), request.getTriggerMode()));
        }

        apiCaseResultService.batchSave(executeQueue);

        String reportType = request.getConfig().getReportType();
        String poolId = request.getConfig().getResourcePoolId();
        DBTestQueue deQueue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.DEFINITION.name(), serialReportId, reportType, ApiRunMode.DEFINITION.name(), request.getConfig());
        // 开始选择执行模式
        if (deQueue != null && deQueue.getQueue() != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("API-CASE-RUN");
                    if (request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                        apiScenarioSerialService.serial(deQueue, deQueue.getQueue());
                    } else {
                        apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), deQueue, ApiRunMode.DEFINITION.name());
                    }
                }
            });
            thread.start();
        }
        return responseDTOS;
    }
}
