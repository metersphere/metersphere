package io.metersphere.api.parser;

import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.parser.api.PostmanParser;
import io.metersphere.api.parser.api.Swagger3Parser;
import org.apache.commons.lang3.StringUtils;

public class ImportParserFactory {
    public static ImportParser<?> getImportParser(String platform) {
        if (StringUtils.equals(ApiImportPlatform.Swagger3.name(), platform)) {
            return new Swagger3Parser<>();
        } else if (StringUtils.equals(ApiImportPlatform.Postman.name(), platform)) {
            return new PostmanParser<>();
        }
        return null;
    }
}
