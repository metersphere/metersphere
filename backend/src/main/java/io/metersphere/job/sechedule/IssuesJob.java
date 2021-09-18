package io.metersphere.job.sechedule;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.base.domain.TestPlanTestCaseWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.service.TestCaseIssueService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class IssuesJob {
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    private TestCaseIssueService testCaseIssueService;

    //@QuartzScheduled(fixedDelay = 3600 * 1000)
//    @Scheduled(fixedDelay = 120 * 1000)
    public void issuesCount() {
        LogUtil.info("测试计划-测试用例同步缺陷信息开始");
        int pageSize = 100;
        int pages = 1;
        for (int i = 0; i < pages; i++) {
            Page<List<TestPlanTestCase>> page = PageHelper.startPage(i, pageSize, true);
            List<TestPlanTestCaseWithBLOBs> list = testPlanTestCaseService.listAll();
            pages = page.getPages();// 替换成真实的值
            list.forEach(l -> {
                testCaseIssueService.updateIssuesCount(l.getCaseId());
            });
        }
        LogUtil.info("测试计划-测试用例同步缺陷信息结束");
    }
}

