package io.metersphere.sdk.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleOpenApiConfig {
    private static final String prePackages = "io.metersphere.";

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system-setting")
                .packagesToScan(prePackages + "system")
                .build();
    }

    @Bean
    public GroupedOpenApi projectApi() {
        return GroupedOpenApi.builder()
                .group("project-management")
                .packagesToScan(prePackages + "project")
                .build();
    }

    @Bean
    public GroupedOpenApi apiTestApi() {
        return GroupedOpenApi.builder()
                .group("api-test")
                .packagesToScan(prePackages + "api")
                .build();
    }

    @Bean
    public GroupedOpenApi bugApi() {
        return GroupedOpenApi.builder()
                .group("bug-management")
                .packagesToScan(prePackages + "bug")
                .build();
    }

    @Bean
    public GroupedOpenApi caseApi() {
        return GroupedOpenApi.builder()
                .group("case-management")
                .packagesToScan(prePackages + "functional")
                .build();
    }

    @Bean
    public GroupedOpenApi loadApi() {
        return GroupedOpenApi.builder()
                .group("load-test")
                .packagesToScan(prePackages + "load")
                .build();
    }


    @Bean
    public GroupedOpenApi planApi() {
        return GroupedOpenApi.builder()
                .group("test-plan")
                .packagesToScan(prePackages + "plan")
                .build();
    }

    @Bean
    public GroupedOpenApi uiApi() {
        return GroupedOpenApi.builder()
                .group("ui-test")
                .packagesToScan(prePackages + "ui")
                .build();
    }

    @Bean
    public GroupedOpenApi workstationApi() {
        return GroupedOpenApi.builder()
                .group("workstation")
                .packagesToScan(prePackages + "workstation")
                .build();
    }

    @Bean
    public GroupedOpenApi xpackApi() {
        return GroupedOpenApi.builder()
                .group("xpack")
                .packagesToScan(prePackages + "xpack")
                .build();
    }

    @Bean
    public GroupedOpenApi sdkApi() {
        return GroupedOpenApi.builder()
                .group("sdk")
                .packagesToScan(prePackages + "sdk")
                .build();
    }
}
