package io.metersphere.job.sechedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.TestPlanTestCaseWithBLOBs;
import io.metersphere.track.service.IssuesService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

@Component
public class IssuesJob {
    @Resource
    private IssuesService issuesService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;

    @QuartzScheduled(fixedDelay = 3600 * 1000)
    //@Scheduled(fixedDelay = 120 * 1000)
    public void IssuesCount() {
        /*int pageSize = 100;
        int pages = 0;
        Page<List<TestPlanTestCase>> page = PageHelper.startPage(pages, pageSize, true);
        pages = page.getPages();
        for (int i = 0; i < pages; i++) {*/
        List<TestPlanTestCaseWithBLOBs> list = testPlanTestCaseService.listAll();
        list.forEach(l -> {
            List<IssuesDao> issues = issuesService.getIssues(l.getCaseId());
            int issuesCount = issues.size();
            testPlanTestCaseService.updateIssues(issuesCount, l.getPlanId(), l.getCaseId(), issues.toString());
        });
    }
    }

