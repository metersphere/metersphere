package io.metersphere.listener;

import io.metersphere.api.dto.shell.filter.ScriptFilter;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.ProjectClassLoader;
import io.metersphere.service.*;
import io.metersphere.service.definition.ApiModuleService;
import io.metersphere.service.scenario.ApiScenarioModuleService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.python.core.Options;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApiAppStartListener implements ApplicationRunner {
    @Resource
    private BaseScheduleService scheduleService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private MockConfigService mockConfigService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private PluginService pluginService;
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiModuleService apiModuleService;
    @Resource
    private ApiScenarioModuleService apiScenarioModuleService;

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
    public void run(ApplicationArguments args) throws Exception {
        LogUtil.info("================= API 应用启动 =================");
        System.setProperty("jmeter.home", jmeterHome);

        LogUtil.info("初始化安全过滤脚本");
        ScriptFilter.initScript(ScriptFilter.beanshell);
        ScriptFilter.initScript(ScriptFilter.python);
        ScriptFilter.initScript(ScriptFilter.groovy);

        LogUtil.info("加载自定义插件");
        pluginService.loadPlugins();

        LogUtil.info("处理重启导致未执行完成的报告");
        apiExecutionQueueService.exceptionHandling();

        LogUtil.info("初始化默认项目接口模块");
        apiModuleService.initDefaultNode();

        LogUtil.info("初始化默认项目场景模块");
        apiScenarioModuleService.initDefaultModule();

        LogUtil.info("导入内置python包处理");
        initPythonEnv();

        //检查状态为开启的TCP-Mock服务端口
        LogUtil.info("starting check mock ");
        mockConfigService.initMockTcpService();

        LogUtil.info("starting quartz");
        StringBuffer buffer = new StringBuffer("定时任务相关设置：");
        buffer.append("quartz.acquireTriggersWithinLock :")
                .append(acquireTriggersWithinLock).append(StringUtils.LF)
                .append("quartz.enabled ")
                .append(quartzEnable).append(StringUtils.LF)
                .append("quartz.scheduler-name ")
                .append(quartzScheduleName).append(StringUtils.LF)
                .append("quartz.thread-count ")
                .append(quartzThreadCount).append(StringUtils.LF);

        LogUtil.info(buffer.toString());
        scheduleService.startEnableSchedules(ScheduleGroup.API_SCENARIO_TEST);
        scheduleService.startEnableSchedules(ScheduleGroup.SWAGGER_IMPORT);
        LogUtil.info("Startup complete");

        ProjectClassLoader.initClassLoader();
    }


    /**
     * 解决接口测试-无法导入内置python包
     */
    private void initPythonEnv() {
        //解决无法加载 PyScriptEngineFactory
        try {
            Options.importSite = false;
            PythonInterpreter interpreter = new PythonInterpreter();
            StringBuffer pathBuffer = new StringBuffer(jMeterService.getJmeterHome());
            pathBuffer.append("/lib/ext/jython-standalone.jar/Lib");
            interpreter.exec("import sys");
            interpreter.exec("sys.path.append(\"" + pathBuffer.toString() + "\")");
            LogUtil.info("sys.path: ", pathBuffer.toString());
        } catch (Exception e) {
            LogUtil.error("导入内置python包处理异常 ", e);
        }
    }
}
