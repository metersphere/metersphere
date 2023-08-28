package io.metersphere.system.job;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.system.service.UserInviteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 定期清理用户邀请记录
 */
@Component
public class CleanUserInviteJob {

    @Resource
    private UserInviteService userInviteService;

    @QuartzScheduled(cron = "0 0 0 * * ?")
    public void cleanUserInvite() {
        userInviteService.deleteByOneDayAgo();
    }
}
