package io.metersphere.job.sechedule;

import com.alibaba.fastjson.JSON;
import com.fit2cloud.quartz.anno.QuartzScheduled;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.base.domain.TestPlanTestCaseWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.service.IssuesService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class IssuesJob {
    @Resource
    private IssuesService issuesService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;

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
                try {
                    List<IssuesDao> issues = issuesService.getIssues(l.getCaseId());
                    if (CollectionUtils.isEmpty(issues)) {
                        return;
                    }
                    int issuesCount = issues.size();
                    testPlanTestCaseService.updateIssues(issuesCount, l.getPlanId(), l.getCaseId(), JSON.toJSONString(issues));
                } catch (Exception e) {
                    LogUtil.error("定时任务处理bug数量报错planId: {}, message: {}", l.getPlanId(), ExceptionUtils.getStackTrace(e));
                }
            });
        }
        LogUtil.info("测试计划-测试用例同步缺陷信息结束");
    }
}

