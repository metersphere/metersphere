package io.metersphere.api.parser;

import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.parser.api.HarParserApiDefinition;
import io.metersphere.api.parser.api.MetersphereParserApiDefinition;
import io.metersphere.api.parser.api.PostmanParserApiDefinition;
import io.metersphere.api.parser.api.Swagger3ParserApiDefinition;
import org.apache.commons.lang3.StringUtils;

public class ImportParserFactory {
    public static ApiDefinitionImportParser<?> getImportParser(String platform) {
        if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Swagger3.name(), platform)) {
            return new Swagger3ParserApiDefinition();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Postman.name(), platform)) {
            return new PostmanParserApiDefinition();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.MeterSphere.name(), platform)) {
            return new MetersphereParserApiDefinition();
        } else if (StringUtils.equalsIgnoreCase(ApiImportPlatform.Har.name(), platform)) {
            return new HarParserApiDefinition();
        }
        return null;
    }
}