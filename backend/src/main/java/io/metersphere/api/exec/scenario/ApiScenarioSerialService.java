package io.metersphere.api.exec.scenario;

import io.metersphere.api.dto.RunModeDataDTO;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.exec.queue.SerialBlockingQueueUtil;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.HashTreeUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiScenarioSerialService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;

    public void serial(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request, String serialReportId, List<MsExecResponseDTO> responseDTOS) {
        LoggerUtil.debug("Scenario run-执行脚本装载-进入串行准备");

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioReportMapper batchMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
        // 非集合报告，先生成执行队列
        if (StringUtils.isEmpty(serialReportId)) {
            for (String reportId : executeQueue.keySet()) {
                APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                report.setStatus(APITestStatus.Waiting.name());
                batchMapper.insert(report);
                responseDTOS.add(new MsExecResponseDTO(executeQueue.get(reportId).getTestId(), reportId, request.getRunMode()));
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
        // 开始串行执行
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Scenario串行执行线程");
                try {
                    //记录串行执行中的环境参数，供下一个场景执行时使用。 <envId,<key,data>>
                    Map<String, Map<String, String>> executeEnvParams = new LinkedHashMap<>();
                    ApiTestEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                    HashTreeUtil hashTreeUtil = new HashTreeUtil();
                    for (String key : executeQueue.keySet()) {
                        APIScenarioReportResult report = executeQueue.get(key).getReport();
                        if (StringUtils.isEmpty(serialReportId)) {
                            report.setStatus(APITestStatus.Running.name());
                            report.setCreateTime(System.currentTimeMillis());
                            report.setUpdateTime(System.currentTimeMillis());
                            apiScenarioReportMapper.updateByPrimaryKey(report);
                        }

                        String queueReportId = StringUtils.isNotEmpty(serialReportId) ? serialReportId + "_" + executeQueue.get(key).getTestId() : key;
                        LoggerUtil.info("Scenario run-开始执行，队列ID：【 " + queueReportId + " 】");
                        try {
                            if (!executeEnvParams.isEmpty()) {
                                HashTree hashTree = executeQueue.get(key).getHashTree();
                                hashTreeUtil.setEnvParamsMapToHashTree(hashTree, executeEnvParams);
                                executeQueue.get(key).setHashTree(hashTree);
                            }

                            SerialBlockingQueueUtil.init(queueReportId, 1);

                            String reportId = StringUtils.isNotEmpty(serialReportId) ? serialReportId : key;
                            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(executeQueue.get(key).getTestId(), reportId, request.getRunMode(), executeQueue.get(key).getHashTree());
                            if (request.getConfig() != null) {
                                runRequest.setConfig(request.getConfig());
                            }
                            runRequest.setReportType(StringUtils.isNotEmpty(serialReportId) ? RunModeConstants.SET_REPORT.toString() : RunModeConstants.INDEPENDENCE.toString());
                            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig()));
                            runRequest.setTestPlanReportId(request.getTestPlanReportId());

                            // 开始执行
                            jMeterService.run(runRequest);

                            Object reportObj = SerialBlockingQueueUtil.take(queueReportId);
                            LoggerUtil.info("Scenario run-执行完成：【 " + queueReportId + " 】");
                            // 如果开启失败结束执行，则判断返回结果状态
                            if (request.getConfig().isOnSampleError()) {
                                if (reportObj != null) {
                                    ApiScenarioReport scenarioReport = (ApiScenarioReport) reportObj;
                                    if (!scenarioReport.getStatus().equals("Success")) {
                                        break;
                                    }
                                }
                            }

                            Map<String, Map<String, String>> envParamsMap = hashTreeUtil.getEnvParamsDataByHashTree(executeQueue.get(key).getHashTree(), apiTestEnvironmentService);
                            executeEnvParams = hashTreeUtil.mergeParamDataMap(executeEnvParams, envParamsMap);
                        } catch (Exception e) {
                            SerialBlockingQueueUtil.remove(queueReportId);
                            LogUtil.error("执行终止：" + e.getMessage());
                            break;
                        }
                    }
                    // 更新集成报告
                    if (StringUtils.isNotEmpty(serialReportId)) {
                        apiScenarioReportService.margeReport(serialReportId);
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        });
        thread.start();
    }
}
