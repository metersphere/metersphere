package io.metersphere.api.exec.perf;

import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class PerfExecService {
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private PerfModeExecService perfModeExecService;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;

    public Map<String, String> run(String planReportId, RunModeConfigDTO config, String triggerMode, Map<String, String> perfMap) {
        LoggerUtil.info("开始生成测试计划性能测试队列");

        List<RunTestPlanRequest> requests = new LinkedList<>();
        Map<String, String> responseMap = new LinkedHashMap<>();
        perfMap.forEach((k, v) -> {
            String reportId = null;
            // 执行中的性能测试资源
            List<String> reportIds = extLoadTestReportMapper.selectReportIdByTestId(v);
            if (CollectionUtils.isNotEmpty(reportIds)) {
                reportId = reportIds.get(0);
            }
            // 过滤掉执行中的性能测试资源
            if (StringUtils.isEmpty(reportId)) {
                RunTestPlanRequest request = new RunTestPlanRequest();
                reportId = UUID.randomUUID().toString();
                request.setId(v);
                request.setTestPlanLoadId(k);
                request.setReportId(reportId);
                request.setTriggerMode(triggerMode);
                if (StringUtils.isNotBlank(config.getResourcePoolId())) {
                    request.setTestResourcePoolId(config.getResourcePoolId());
                }
                if (StringUtils.isEmpty(triggerMode)) {
                    request.setTriggerMode(ReportTriggerMode.TEST_PLAN_SCHEDULE.name());
                }
                requests.add(request);
            }
            responseMap.put(k, reportId);
        });

        //将性能测试加入到队列中
        if (MapUtils.isNotEmpty(responseMap)) {
            apiExecutionQueueService.add(responseMap, config.getResourcePoolId(), ApiRunMode.TEST_PLAN_PERFORMANCE_TEST.name(),
                    planReportId, config.getReportType(), config.getMode(), config);
        }
        if (CollectionUtils.isEmpty(requests) && StringUtils.isNotEmpty(planReportId)) {
            apiExecutionQueueService.testPlanReportTestEnded(planReportId);
            return responseMap;
        }
        LoggerUtil.info("开始执行性能测试任务");
        if (config != null && config.getMode().equals(RunModeConstants.SERIAL.toString())) {
            perfModeExecService.serial(requests.get(0));
        } else {
            perfModeExecService.parallel(requests);
        }
        return responseMap;
    }
}
