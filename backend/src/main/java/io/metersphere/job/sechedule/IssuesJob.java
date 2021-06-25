package io.metersphere.job.sechedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.TestPlanTestCase;
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
    //@Scheduled(fixedDelay = 3600 * 1000)
    public void IssuesCount() {
        int pageSize = 20;
        int pages = 1;
        for (int i = 0; i < pages; i++) {
            Page<List<TestPlanTestCase>> page = PageHelper.startPage(i, pageSize, true);
            pages = page.getPages();
            List<TestPlanTestCaseWithBLOBs> list = testPlanTestCaseService.listAll();
            list.forEach(l -> {
                List<IssuesDao> issues = issuesService.getIssues(l.getCaseId());
                int issuesCount = issues.size();
                testPlanTestCaseService.updateIssues(issuesCount, l.getPlanId(), l.getCaseId(), issues.toString());
            });
        }
    }
}
