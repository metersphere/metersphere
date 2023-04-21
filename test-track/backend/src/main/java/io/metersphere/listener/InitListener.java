package io.metersphere.listener;

import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.RunInterface;
import io.metersphere.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class InitListener implements ApplicationRunner {

    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private IssuesService issuesService;
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private TestReviewTestCaseService testReviewTestCaseService;
    @Resource
    private CustomFieldResourceCompatibleService customFieldResourceCompatibleService;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private BaseScheduleService baseScheduleService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        this.initOnceOperate();
        platformPluginService.loadPlatFormPlugins();

        baseScheduleService.startEnableSchedules(ScheduleGroup.ISSUE_SYNC);
        baseScheduleService.startEnableSchedules(ScheduleGroup.TEST_PLAN_TEST);
    }

    /**
     * 处理初始化数据、兼容数据
     * 只在第一次升级的时候执行一次
     *
     * @param initFuc
     * @param key
     */
    private void initOnceOperate(RunInterface initFuc, final String key) {
        try {
            String value = systemParameterService.getValue(key);
            if (StringUtils.isBlank(value)) {
                initFuc.run();
                systemParameterService.saveInitParam(key);
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private void initOnceOperate() {
        initOnceOperate(issuesService::syncThirdPartyIssues, "init.issue");
        initOnceOperate(issuesService::issuesCount, "init.issueCount");
        initOnceOperate(testCaseService::initOrderField, "init.sort.test.case");
        initOnceOperate(testReviewTestCaseService::initOrderField, "init.sort.review.test.case");
        initOnceOperate(customFieldResourceCompatibleService::compatibleData, "init.custom.field.resource");
        initOnceOperate(testCaseService::initAttachment, "init.test.case.attachment");
    }
}
