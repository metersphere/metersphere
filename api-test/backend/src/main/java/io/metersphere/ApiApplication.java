package io.metersphere;

import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaProperties;
import io.metersphere.config.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        QuartzAutoConfiguration.class,
        LdapAutoConfiguration.class,
        Neo4jAutoConfiguration.class
})
@PropertySource(value = {
        "classpath:commons.properties",
        "file:/opt/metersphere/conf/metersphere.properties",
}, encoding = "UTF-8", ignoreResourceNotFound = true)
@ServletComponentScan
@EnableConfigurationProperties({
        KafkaProperties.class,
        MinioProperties.class,
        JmeterProperties.class
})
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
