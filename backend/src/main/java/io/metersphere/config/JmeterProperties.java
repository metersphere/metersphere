package io.metersphere.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JmeterProperties.JMETER_PREFIX)
@Setter
@Getter
public class JmeterProperties {

    public static final String JMETER_PREFIX = "jmeter";

    private String image;

    private String home;

    private String heap = "-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m";
    private String gcAlgo = "-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1ReservePercent=20";
}
