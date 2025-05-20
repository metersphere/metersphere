package io.metersphere.api;

import io.metersphere.api.config.JmeterProperties;
import io.metersphere.system.config.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        QuartzAutoConfiguration.class,
        LdapAutoConfiguration.class,
        Neo4jAutoConfiguration.class,
})
@EnableConfigurationProperties({
        MinioProperties.class,
        JmeterProperties.class
})
@ServletComponentScan
@ComponentScan(basePackages = {"io.metersphere.sdk","io.metersphere.ai", "io.metersphere.api", "io.metersphere.system", "io.metersphere.project"})
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
