package io.metersphere;

import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication(exclude = {QuartzAutoConfiguration.class})
@ServletComponentScan
@EnableConfigurationProperties({
        KafkaProperties.class,
        JmeterProperties.class
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
