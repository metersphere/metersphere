package io.metersphere.listener;

import io.metersphere.commons.utils.LogUtil;
import org.apache.jmeter.util.JMeterUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProjectAppStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LogUtil.info("================= PROJECT 应用启动 =================");
        this.initJmeterHome();
    }
    private void initJmeterHome() {
        String JMETER_HOME = Objects.requireNonNull(getClass().getResource("/")).getPath() + "jmeter";
        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }
}
