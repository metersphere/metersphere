package io.metersphere.system.job;


import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.sdk.constants.ParamConstants;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.mapper.BaseOperationLogMapper;
import io.metersphere.system.mapper.SystemParameterMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Component
public class CleanLogJob {

    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private BaseOperationLogMapper baseOperationLogMapper;

    private static final int DEFAULT_TIME = 6;


    /**
     * 清理日志 每天凌晨一点执行
     */
    @QuartzScheduled(cron = "0 0 1 * * ?")
    public void cleanupLog() {
        LogUtils.info("clean up log start.");
        SystemParameter parameter = systemParameterMapper.selectByPrimaryKey(ParamConstants.CleanConfig.OPERATION_LOG.getValue());
        Optional.ofNullable(parameter).ifPresentOrElse(
                p -> {
                    String type = p.getParamValue().substring(p.getParamValue().length() - 1);
                    String time = p.getParamValue().substring(0, p.getParamValue().length() - 1);
                    switch (type) {
                        case "D" -> {
                            //日
                            LocalDate day = LocalDate.now().minusDays(Integer.parseInt(time));
                            long dayTimestamp = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            this.doCleanupLog(dayTimestamp);
                        }
                        case "M" -> {
                            //月
                            LocalDate month = LocalDate.now().minusMonths(Integer.parseInt(time));
                            long monthTimestamp = month.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            this.doCleanupLog(monthTimestamp);
                        }
                        case "Y" -> {
                            //年
                            LocalDate year = LocalDate.now().minusYears(Integer.parseInt(time));
                            long yearTimestamp = year.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            this.doCleanupLog(yearTimestamp);
                        }
                        default -> {
                        }
                    }
                },
                () -> {
                    LocalDate date = LocalDate.now().minusMonths(DEFAULT_TIME);
                    long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    this.doCleanupLog(timestamp);
                }
        );
        LogUtils.info("clean up log end.");
    }

    private void doCleanupLog(long timestamp) {
        //删除日志
        baseOperationLogMapper.deleteByTime(timestamp);
    }
}