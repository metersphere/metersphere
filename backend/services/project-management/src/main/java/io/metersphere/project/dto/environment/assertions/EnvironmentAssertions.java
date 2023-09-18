package io.metersphere.project.dto.environment.assertions;

import io.metersphere.project.dto.environment.assertions.document.MsAssertionDocument;
import io.metersphere.project.dto.environment.http.ApplicationModule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class EnvironmentAssertions {
    @Schema(description = "应用模块")
    private ApplicationModule module;
    @Schema(description = "xpath类型 xml/html")
    private String xpathType;
    @Schema(description = "正则断言")
    private List<EnvAssertionRegex> regex;
    @Schema(description = "jsonPath断言")
    private List<EnvAssertionJsonPath> jsonPath;
    @Schema(description = "jsr223断言")
    private List<EnvAssertionJSR223> jsr223;
    @Schema(description = "xpath断言")
    private List<EnvAssertionXPath> xpath;
    @Schema(description = "响应时间断言")
    private EnvAssertionDuration duration;
    @Schema(description = "文档断言")
    private MsAssertionDocument document;



}
