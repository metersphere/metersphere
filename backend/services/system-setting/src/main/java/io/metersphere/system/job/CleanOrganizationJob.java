package io.metersphere.system.job;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.OrganizationExample;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.service.OrganizationService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * @author song-cc-rock
 */
@Component
public class CleanOrganizationJob {

    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private OrganizationService organizationService;
    /**
     * 凌晨3点清理删除的组织
     */
    @QuartzScheduled(cron = "0 0 3 * * ?")
    public void cleanOrganization() {
        LogUtils.info("clean up organization start.");
        try {
            LocalDateTime dateTime = LocalDateTime.now().minusDays(30);
            long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.doCleanupOrganization(timestamp);
        } catch (Exception e) {
            LogUtils.error("clean up organization error.", e);
        }
        LogUtils.info("clean up organization end.");
    }

    private void doCleanupOrganization(long timestamp) {
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andDeletedEqualTo(true).andDeleteTimeLessThanOrEqualTo(timestamp);
        List<Organization> organizations = organizationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(organizations)) {
            return;
        }

        organizations.forEach(organization -> organizationService.deleteOrganization(organization.getId()));
    }
}
