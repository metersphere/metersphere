/**
 *
 */
package io.metersphere.api.service.task;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;

import java.util.concurrent.Callable;

public class SerialScenarioExecTask<T> implements Callable<T> {
    private RunScenarioRequest request;
    private JMeterService jMeterService;
    private ApiScenarioReportMapper apiScenarioReportMapper;
    private HashTree hashTree;
    ApiScenarioReport report = null;
    private String id;

    public SerialScenarioExecTask(JMeterService jMeterService, ApiScenarioReportMapper apiScenarioReportMapper, String id, HashTree hashTree, RunScenarioRequest request) {
        this.jMeterService = jMeterService;
        this.apiScenarioReportMapper = apiScenarioReportMapper;
        this.request = request;
        this.hashTree = hashTree;
        this.id = id;
    }

    @Override
    public T call() {
        try {
            if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                jMeterService.runTest(id, hashTree, request.getRunMode(), false, request.getConfig());
            } else {
                jMeterService.runSerial(id, hashTree, request.getReportId(), request.getRunMode(), request.getConfig());
            }
            // 轮询查看报告状态，最多200次，防止死循环
            int index = 1;
            while (index < 200) {
                Thread.sleep(3000);
                index++;
                report = apiScenarioReportMapper.selectByPrimaryKey(id);
                if (report != null && !report.getStatus().equals(APITestStatus.Running.name())) {
                    break;
                }
            }
            // 执行失败了，恢复报告状态
            if (index == 200 && report != null && report.getStatus().equals(APITestStatus.Running.name())) {
                report.setStatus(APITestStatus.Error.name());
                apiScenarioReportMapper.updateByPrimaryKey(report);
            }
            return (T) report;
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            MSException.throwException(ex.getMessage());
            return null;
        }
    }
}