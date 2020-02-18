package io.metersphere.runner.jmx.client;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class JmeterProperties extends Properties {
    private final static Logger logger = LoggerFactory.getLogger(JmeterProperties.class);

    private final String jmeterHome;

    public JmeterProperties(String jmeterHome) {
        this.jmeterHome = jmeterHome;
    }

    public void initJmeterProperties() {
        JMeterUtils.loadJMeterProperties(getJmeterHomeBin() + File.separator + "jmeter.properties");
        JMeterUtils.setJMeterHome(getJmeterHome());
        JMeterUtils.initLocale();

        Properties jmeterProps = JMeterUtils.getJMeterProperties();

        // Add local JMeter properties, if the file is found
        String userProp = JMeterUtils.getPropDefault("user.properties", "");
        if (userProp.length() > 0) {
            File file = JMeterUtils.findFile(userProp);
            if (file.canRead()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Properties tmp = new Properties();
                    tmp.load(fis);
                    jmeterProps.putAll(tmp);
                } catch (IOException e) {
                    logger.error("Failed to init jmeter properties", e);
                }
            }
        }

        // Add local system properties, if the file is found
        String sysProp = JMeterUtils.getPropDefault("system.properties", "");
        if (sysProp.length() > 0) {
            File file = JMeterUtils.findFile(sysProp);
            if (file.canRead()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    System.getProperties().load(fis);
                } catch (IOException e) {
                    logger.error("Failed to init jmeter properties", e);
                }
            }
        }

        jmeterProps.put("jmeter.version", JMeterUtils.getJMeterVersion());
        for (Map.Entry<Object, Object> entry : jmeterProps.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    private String getJmeterHome() {
        return jmeterHome;
    }

    private String getJmeterHomeBin() {
        return getJmeterHome() + File.separator + "bin";
    }
}