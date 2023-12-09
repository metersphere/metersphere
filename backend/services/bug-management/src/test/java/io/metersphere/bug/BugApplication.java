package io.metersphere.bug;

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
        Neo4jAutoConfiguration.class
})
@EnableConfigurationProperties({
        MinioProperties.class
})
@ServletComponentScan
@ComponentScan(basePackages = {"io.metersphere.sdk", "io.metersphere.system", "io.metersphere.project", "io.metersphere.bug"})
public class BugApplication {
    public static void main(String[] args) {
        SpringApplication.run(BugApplication.class, args);
    }
}
