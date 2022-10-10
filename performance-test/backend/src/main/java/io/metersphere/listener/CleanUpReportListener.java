package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.PerformanceReportService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class CleanUpReportListener {
    private static final String UNIT_DAY = "D";
    private static final String UNIT_MONTH = "M";
    private static final String UNIT_YEAR = "Y";
    LocalDate localDate;
    public static final String CONSUME_ID = "clean-up-report";

    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private PerformanceReportService performanceReportService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.CLEAN_UP_REPORT_SCHEDULE, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        localDate = LocalDate.now();
        ProjectConfig config = baseProjectApplicationService.getProjectConfig(projectId);
        LogUtil.info("performance service consume clean_up message, clean able: " + config.getCleanLoadReport());
        if (BooleanUtils.isTrue(config.getCleanLoadReport())) {
            this.cleanUpReport(projectId, config.getCleanLoadReportExpr());
        }
    }

    private void cleanUpReport(String projectId, String expr) {
        LogUtil.info("clean up performance report start.");
        long time = getCleanDate(expr);
        if (time == 0) {
            return;
        }
        performanceReportService.cleanUpReport(time, projectId);
        LogUtil.info("clean up performance report end.");
    }

    private long getCleanDate(String expr) {
        LocalDate date = null;
        long timeMills = 0;
        if (StringUtils.isNotBlank(expr)) {
            try {
                String unit = expr.substring(expr.length() - 1);
                int quantity = Integer.parseInt(expr.substring(0, expr.length() - 1));
                if (StringUtils.equals(unit, UNIT_DAY)) {
                    date = localDate.minusDays(quantity);
                } else if (StringUtils.equals(unit, UNIT_MONTH)) {
                    date = localDate.minusMonths(quantity);
                } else if (StringUtils.equals(unit, UNIT_YEAR)) {
                    date = localDate.minusYears(quantity);
                } else {
                    LogUtil.error("clean up expr parse error. expr : " + expr);
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                LogUtil.error("clean up job. get clean date error.");
            }
        }
        if (date != null) {
            timeMills = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return timeMills;
    }
}
