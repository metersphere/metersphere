package io.metersphere.api.parser;

import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.parser.api.dataimport.*;
import org.apache.commons.lang3.StringUtils;

public class ImportParserFactory {
    public static ApiDefinitionImportParser<?> getApiDefinitionImportParser(String platform) {
        if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Swagger3.name(), platform)) {
            return new Swagger3ParserApiDefinition();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Postman.name(), platform)) {
            return new PostmanParserApiDefinition();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.MeterSphere.name(), platform)) {
            return new MetersphereParserApiDefinition();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Har.name(), platform)) {
            return new HarParserApiDefinition();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Jmeter.name(), platform)) {
            return new JmeterParserApiDefinition();
        }
        return null;
    }

    public static ApiScenarioImportParser getApiScenarioImportParser(String platform) {
        if (StringUtils.equalsIgnoreCase(ApiImportPlatform.MeterSphere.name(), platform)) {
            return new MetersphereParserApiScenario();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Jmeter.name(), platform)) {
            return new JmeterParserApiScenario();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Har.name(), platform)) {
            return new HarParserApiScenario();
        }
        return null;
    }
}