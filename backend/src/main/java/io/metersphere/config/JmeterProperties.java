package io.metersphere.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JmeterProperties.JMETER_PREFIX)
public class JmeterProperties {

    public static final String JMETER_PREFIX = "jmeter";

    private String image = "registry.fit2cloud.com/metersphere/jmeter-master:0.0.3";

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
