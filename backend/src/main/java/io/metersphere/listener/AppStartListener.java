package io.metersphere.listener;

import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.ScheduleService;
import org.python.core.Options;
import org.python.util.PythonInterpreter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AppStartListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private JMeterService jMeterService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        System.out.println("================= 应用启动 =================");

        initPythonEnv();

        try {
            Thread.sleep(3 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduleService.startEnableSchedules();
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
}
