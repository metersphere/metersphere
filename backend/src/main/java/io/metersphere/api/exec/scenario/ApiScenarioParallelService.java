package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class ApiScenarioParallelService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private JMeterService jMeterService;

    public void parallel(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request, String serialReportId, List<MsExecResponseDTO> responseDTOS, DBTestQueue executionQueue) {
        if (StringUtils.isEmpty(serialReportId)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiScenarioReportMapper batchMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
            // 开始并发执行
            for (String reportId : executeQueue.keySet()) {
                //存储报告
                APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                batchMapper.insert(report);
                responseDTOS.add(new MsExecResponseDTO(executeQueue.get(reportId).getTestId(), reportId, request.getRunMode()));
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
        for (String reportId : executeQueue.keySet()) {
            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(executeQueue.get(reportId).getTestId(), StringUtils.isNotEmpty(serialReportId) ? serialReportId : reportId, request.getRunMode(), executeQueue.get(reportId).getHashTree());
            runRequest.setReportType(StringUtils.isNotEmpty(serialReportId) ? RunModeConstants.SET_REPORT.toString() : RunModeConstants.INDEPENDENCE.toString());
            runRequest.setQueueId(executionQueue.getId());
            if (request.getConfig() != null) {
                runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()));
                runRequest.setPoolId(request.getConfig().getResourcePoolId());
            }
            runRequest.setTestPlanReportId(request.getTestPlanReportId());
            runRequest.setHashTree(executeQueue.get(reportId).getHashTree());
            runRequest.setPlatformUrl(executionQueue.getDetailMap().get(reportId));
            runRequest.setRunType(RunModeConstants.PARALLEL.toString());
            if (LoggerUtil.getLogger().isDebugEnabled()) {
                LoggerUtil.debug("Scenario run-开始并发执行：" + JSON.toJSONString(request));
            }
            jMeterService.run(runRequest);
        }
    }
}
