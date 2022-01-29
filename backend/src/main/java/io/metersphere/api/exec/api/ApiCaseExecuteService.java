package io.metersphere.api.exec.api;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiCaseRunRequest;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.exec.utils.ApiDefinitionExecResultUtil;
import io.metersphere.api.service.ApiCaseResultService;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;
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
        planApiCases.forEach(testPlanApiCase -> {
            ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.addResult(request, testPlanApiCase, status);
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
        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            LoggerUtil.debug("开始串行执行");
            if (deQueue != null && deQueue.getQueue() != null) {
                apiScenarioSerialService.serial(deQueue, deQueue.getQueue());
            }
        } else {
            LoggerUtil.debug("开始并发执行");
            if (deQueue != null && deQueue.getQueue() != null) {
                apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), deQueue, runMode);
            }
        }
        return responseDTOS;
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
        List<ApiTestCaseWithBLOBs> list = apiTestCaseMapper.selectByExampleWithBLOBs(example);
        LoggerUtil.debug("查询到执行数据：" + list.size());

        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            if (request.getCondition() == null || !request.getCondition().isSelectAll()) {
                // 按照id指定顺序排序
                FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(request.getIds());
                fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
                BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
                Collections.sort(list, beanComparator);
            }
        }

        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ApiRunMode.DEFINITION.name());
        }

        List<MsExecResponseDTO> responseDTOS = new LinkedList<>();
        Map<String, ApiDefinitionExecResult> executeQueue = new LinkedHashMap<>();
        String status = request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString()) ? APITestStatus.Waiting.name() : APITestStatus.Running.name();
        list.forEach(caseWithBLOBs -> {
            ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.initBase(caseWithBLOBs.getId(), APITestStatus.Running.name(), null, request.getConfig());
            report.setStatus(status);
            report.setName(caseWithBLOBs.getName());
            report.setVersionId(caseWithBLOBs.getVersionId());
            executeQueue.put(caseWithBLOBs.getId(), report);
            responseDTOS.add(new MsExecResponseDTO(caseWithBLOBs.getId(), report.getId(), request.getTriggerMode()));
        });

        apiCaseResultService.batchSave(executeQueue);

        String reportType = request.getConfig().getReportType();
        String poolId = request.getConfig().getResourcePoolId();
        DBTestQueue deQueue = apiExecutionQueueService.add(executeQueue, poolId, ApiRunMode.DEFINITION.name(), null, reportType, ApiRunMode.DEFINITION.name(), request.getConfig());
        // 开始选择执行模式
        if (request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            LoggerUtil.debug("开始串行执行");
            if (deQueue != null && deQueue.getQueue() != null) {
                apiScenarioSerialService.serial(deQueue, deQueue.getQueue());
            }
        } else {
            LoggerUtil.debug("开始并发执行");
            if (deQueue != null && deQueue.getQueue() != null) {
                apiCaseParallelExecuteService.parallel(executeQueue, request.getConfig(), deQueue, ApiRunMode.DEFINITION.name());
            }
        }
        return responseDTOS;
    }
}
