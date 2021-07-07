package io.metersphere;

import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        QuartzAutoConfiguration.class,
        LdapAutoConfiguration.class,
        Neo4jAutoConfiguration.class
})
@ServletComponentScan
@EnableConfigurationProperties({
        KafkaProperties.class,
        JmeterProperties.class
})
@EnableScheduling
//@PropertySource(value = {"file:c:\\opt\\metersphere\\conf\\metersphere.properties"}, encoding = "UTF-8", ignoreResourceNotFound = true)
@PropertySource(value = {"file:/opt/metersphere/conf/metersphere.properties"}, encoding = "UTF-8", ignoreResourceNotFound = true)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
