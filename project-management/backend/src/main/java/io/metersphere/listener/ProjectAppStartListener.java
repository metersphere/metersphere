package io.metersphere.listener;

import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.BaseScheduleService;
import jakarta.annotation.Resource;
import org.apache.jmeter.util.JMeterUtils;
import org.python.core.Options;
import org.python.util.PythonInterpreter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProjectAppStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private BaseScheduleService baseScheduleService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LogUtil.info("================= PROJECT 应用启动 =================");

        this.initJmeterHome();

        LogUtil.info("导入内置python包处理");
        this.initPythonEnv();

        baseScheduleService.startEnableSchedules(ScheduleGroup.CLEAN_UP_REPORT);
    }

    private void initJmeterHome() {
        String JMETER_HOME = Objects.requireNonNull(getClass().getResource("/")).getPath() + "jmeter";
        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    /**
     * 解决接口测试-无法导入内置python包
     */
    private void initPythonEnv() {
        //解决无法加载 PyScriptEngineFactory
        try {
            Options.importSite = false;
            PythonInterpreter interpreter = new PythonInterpreter();
            StringBuffer pathBuffer = new StringBuffer(getClass().getResource("/").getPath() + "jmeter");
            pathBuffer.append("/lib/ext/jython-standalone.jar/Lib");
            interpreter.exec("import sys");
            interpreter.exec("sys.path.append(\"" + pathBuffer.toString() + "\")");
            LogUtil.info("sys.path: ", pathBuffer.toString());
        } catch (Exception e) {
            LogUtil.error("导入内置python包处理异常 ", e);
        }
    }
}
