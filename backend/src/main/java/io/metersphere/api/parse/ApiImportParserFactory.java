package io.metersphere.api.parse;

import io.metersphere.commons.constants.ApiImportPlatform;
import io.metersphere.commons.constants.FileType;
import io.metersphere.performance.parse.EngineSourceParser;
import io.metersphere.performance.parse.xml.XmlEngineSourceParse;
import org.apache.commons.lang3.StringUtils;

public class ApiImportParserFactory {
    public static ApiImportParser getApiImportParser(String platform) {
        if (StringUtils.equals(ApiImportPlatform.Metersphere.name(), platform)) {
            return new MsParser();
        }
        return null;
    }
}
