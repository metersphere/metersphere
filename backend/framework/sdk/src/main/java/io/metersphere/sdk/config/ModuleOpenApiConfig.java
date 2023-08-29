package io.metersphere.sdk.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleOpenApiConfig {
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system-setting")
                .packagesToScan("io.metersphere.system")
                .build();
    }

    @Bean
    public GroupedOpenApi projectApi() {
        return GroupedOpenApi.builder()
                .group("project-management")
                .packagesToScan("io.metersphere.project")
                .build();
    }

    @Bean
    public GroupedOpenApi apiTestApi() {
        return GroupedOpenApi.builder()
                .group("api-test")
                .packagesToScan("io.metersphere.api")
                .build();
    }

    @Bean
    public GroupedOpenApi bugApi() {
        return GroupedOpenApi.builder()
                .group("bug-management")
                .packagesToScan("io.metersphere.bug")
                .build();
    }

    @Bean
    public GroupedOpenApi caseApi() {
        return GroupedOpenApi.builder()
                .group("case-management")
                .packagesToScan("io.metersphere.functional")
                .build();
    }

    @Bean
    public GroupedOpenApi loadApi() {
        return GroupedOpenApi.builder()
                .group("load-test")
                .packagesToScan("io.metersphere.load")
                .build();
    }


    @Bean
    public GroupedOpenApi planApi() {
        return GroupedOpenApi.builder()
                .group("test-plan")
                .packagesToScan("io.metersphere.plan")
                .build();
    }

    @Bean
    public GroupedOpenApi uiApi() {
        return GroupedOpenApi.builder()
                .group("ui-test")
                .packagesToScan("io.metersphere.ui")
                .build();
    }

    @Bean
    public GroupedOpenApi workstationApi() {
        return GroupedOpenApi.builder()
                .group("workstation")
                .packagesToScan("io.metersphere.workstation")
                .build();
    }

    @Bean
    public GroupedOpenApi xpackApi() {
        return GroupedOpenApi.builder()
                .group("xpack")
                .packagesToScan("io.metersphere.xpack")
                .build();
    }

    @Bean
    public GroupedOpenApi sdkApi() {
        return GroupedOpenApi.builder()
                .group("sdk")
                .packagesToScan("io.metersphere.sdk")
                .build();
    }
}
