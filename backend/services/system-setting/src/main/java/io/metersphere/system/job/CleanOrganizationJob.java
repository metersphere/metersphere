package io.metersphere.system.job;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.OrganizationExample;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class CleanOrganizationJob {

    @Resource
    OrganizationMapper organizationMapper;

    /**
     * 凌晨3点清理删除的组织
     */
    @QuartzScheduled(cron = "0 0 3 * * ?")
    public void cleanOrganization() {
        LoggerUtil.info("clean up organization start.");
        try {
            LocalDate date = LocalDate.now().minusMonths(1);
            long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.doCleanupOrganization(timestamp);
        } catch (Exception e) {
            LoggerUtil.error("clean up organization error.", e);
        }
        LoggerUtil.info("clean up organization end.");
    }

    private void doCleanupOrganization(long timestamp) {
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andDeletedEqualTo(true).andDeleteTimeLessThanOrEqualTo(timestamp);
        List<Organization> organizations = organizationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(organizations)) {
            return;
        }

        organizations.forEach(organization -> {
            // TODO 清理组织下的资源
            // 删除项目
            // 删除用户组, 用户组关系
            // 删除环境组
            // 删除定时任务
            // 操作记录{项目, 组织}
        });
        // 删除组织
        organizationMapper.deleteByExample(example);
    }
}
