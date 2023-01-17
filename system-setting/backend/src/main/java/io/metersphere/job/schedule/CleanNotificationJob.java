package io.metersphere.job.schedule;


import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.base.domain.Notification;
import io.metersphere.base.domain.NotificationExample;
import io.metersphere.base.mapper.NotificationMapper;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lyh
 */
@Component
public class CleanNotificationJob {

    @Resource
    private NotificationMapper notificationMapper;


    @QuartzScheduled(cron = "0 3 0 * * ?")
    public void cleanupNotification() {
        LogUtil.info("clean up notification start.");
        try {
            LocalDate date = LocalDate.now().minusMonths(3);
            long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.doCleanupNotification(timestamp);
        } catch (Exception e) {
            LogUtil.error("clean up notification error.", e);
        }
        LogUtil.info("clean up notification end.");
    }

    private void doCleanupNotification(long timestamp) {
        NotificationExample example = new NotificationExample();
        example.createCriteria().andCreateTimeLessThanOrEqualTo(timestamp);
        List<Notification> notifications = notificationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(notifications)) {
            return;
        }

        List<Long> ids = notifications.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());

        int handleCount = 5000;
        while (ids.size() > handleCount) {
            List<Long> deleteIds = new ArrayList<>(handleCount);
            List<Long> otherIds = new ArrayList<>();
            for (int index = 0; index < ids.size(); index++) {
                if (index < handleCount) {
                    deleteIds.add(ids.get(index));
                } else {
                    otherIds.add(ids.get(index));
                }
            }
            this.deleteNotification(deleteIds);
            ids = otherIds;
        }
        this.deleteNotification(ids);
    }

    private void deleteNotification(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        NotificationExample example = new NotificationExample();
        example.createCriteria().andIdIn(ids);
        notificationMapper.deleteByExample(example);
    }


}
