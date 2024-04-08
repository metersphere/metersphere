package io.metersphere.system.job;


import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.mapper.BaseNotificationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class CleanNotificationJob {

    @Resource
    private BaseNotificationMapper baseNotificationMapper;

    /**
     * 清理90天前的站内通知  每天凌晨三点执行
     */
    @QuartzScheduled(cron = "0 0 3 * * ?")
    public void cleanupNotification() {
        LogUtils.info("clean up notification start.");
        LocalDateTime dateTime = LocalDateTime.now().minusDays(90);
        long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.doCleanupNotification(timestamp);
        LogUtils.info("clean up notification end.");
    }

    private void doCleanupNotification(long timestamp) {
        //获取所有站内通知
        baseNotificationMapper.deleteByTime(timestamp);
    }
}
