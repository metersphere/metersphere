package io.metersphere.listener;

import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.NewDriverManager;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.api.service.MockConfigService;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.RunInterface;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.*;
import io.metersphere.track.service.*;
import org.apache.commons.lang3.StringUtils;
import org.python.core.Options;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class AppStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private JarConfigService jarConfigService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private IssuesService issuesService;
    @Resource
    private ProjectService projectService;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private PluginService pluginService;
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private TestReviewTestCaseService testReviewTestCaseService;
    @Resource
    private MockConfigService mockConfigService;

    @Value("${jmeter.home}")
    private String jmeterHome;
    @Value("${quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock}")
    private String acquireTriggersWithinLock;
    @Value("${quartz.enabled}")
    private boolean quartzEnable;
    @Value("${quartz.scheduler-name}")
    private String quartzScheduleName;
    @Value("${quartz.thread-count}")
    private int quartzThreadCount;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        System.out.println("================= 应用启动 =================");

        System.setProperty("jmeter.home", jmeterHome);

        // 设置并发队列核心数
        BaseSystemConfigDTO dto = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        if (StringUtils.isNotEmpty(dto.getConcurrency())) {
            int size = Integer.parseInt(dto.getConcurrency());
            CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).setCorePoolSize(size);
        }

        loadJars();

        initPythonEnv();

        //检查状态为开启的TCP-Mock服务端口
        projectService.initMockTcpService();

        initOnceOperate();

        pluginService.loadPlugins();

        try {
            Thread.sleep(1 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtil.info("开始启动定时任务。 相关设置：" +
                "quartz.acquireTriggersWithinLock :" + acquireTriggersWithinLock + "\r\n" +
                "quartz.enabled :" + quartzEnable + "\r\n" +
                "quartz.scheduler-name :" + quartzScheduleName + "\r\n" +
                "quartz.thread-count :" + quartzThreadCount + "\r\n"
        );
        scheduleService.startEnableSchedules();
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
        initOnceOperate(apiAutomationService::checkApiScenarioUseUrl, "init.scenario.url");
        initOnceOperate(apiAutomationService::checkApiScenarioReferenceId, "init.scenario.referenceId");
        initOnceOperate(apiAutomationService::initExecuteTimes, "init.scenario.executeTimes");
        initOnceOperate(issuesService::syncThirdPartyIssues, "init.issue");
        initOnceOperate(issuesService::issuesCount, "init.issueCount");
        initOnceOperate(performanceTestService::initScenarioLoadTest, "init.scenario.load.test");
        initOnceOperate(testCaseService::initOrderField, "init.sort.test.case");
        initOnceOperate(apiDefinitionService::initOrderField, "init.sort.api.test.definition");
        initOnceOperate(apiTestCaseService::initOrderField, "init.sort.api.test.case");
        initOnceOperate(apiAutomationService::initOrderField, "init.sort.api.scenario");
        initOnceOperate(performanceTestService::initOrderField, "init.sort.load.case");
        initOnceOperate(testPlanTestCaseService::initOrderField, "init.sort.plan.test.case");
        initOnceOperate(testPlanApiCaseService::initOrderField, "init.sort.plan.api.case");
        initOnceOperate(testPlanScenarioCaseService::initOrderField, "init.sort.plan.api.scenario");
        initOnceOperate(testPlanLoadCaseService::initOrderField, "init.sort.plan.api.load");
        initOnceOperate(testReviewTestCaseService::initOrderField, "init.sort.review.test.case");
        initOnceOperate(apiDefinitionService::initDefaultModuleId, "init.default.module.id");
        initOnceOperate(mockConfigService::initExpectNum, "init.mock.expectNum");
    }

    /**
     * 解决接口测试-无法导入内置python包
     */
    private void initPythonEnv() {
        //解决无法加载 PyScriptEngineFactory
        Options.importSite = false;
        try {
            PythonInterpreter interp = new PythonInterpreter();
            String path = jMeterService.getJmeterHome();
            System.out.println("sys.path: " + path);
            path += "/lib/ext/jython-standalone.jar/Lib";
            interp.exec("import sys");
            interp.exec("sys.path.append(\"" + path + "\")");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 加载jar包
     */
    private void loadJars() {
        List<JarConfig> jars = jarConfigService.list();

        jars.forEach(jarConfig -> {
            try {
                NewDriverManager.loadJar(jarConfig.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error(e.getMessage(), e);
            }
        });

    }
}
