package io.metersphere.api.jmeter;


import io.metersphere.api.exec.queue.SerialBlockingQueueUtil;
import io.metersphere.api.service.MsResultService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class APIBackendListenerHandler {
    public void handleTeardownTest(ResultDTO dto, Map<String, Object> kafkaConfig) {
        LoggerUtil.info("开始处理执行结果报告【" + dto.getReportId() + " 】,资源【 " + dto.getTestId() + " 】");
        TestResultService testResultService = CommonBeanFactory.getBean(TestResultService.class);
        MsResultService resultService = CommonBeanFactory.getBean(MsResultService.class);

        // 无结果返回直接结束队列
        if (dto == null || CollectionUtils.isEmpty(dto.getRequestResults())) {
            SerialBlockingQueueUtil.offer(dto, SerialBlockingQueueUtil.END_SIGN);
        }
        dto.setConsole(resultService.getJmeterLogger(dto.getReportId()));
        // 存储结果
        testResultService.saveResults(dto);

        if (StringUtils.isNotEmpty(dto.getReportId())) {
            SerialBlockingQueueUtil.remove(dto.getReportId());
        }
    }
}
