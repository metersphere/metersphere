package io.metersphere.api.parse;

import io.metersphere.commons.constants.ApiImportPlatform;
import org.apache.commons.lang3.StringUtils;

public class ApiScenarioImportParserFactory {
    public static ApiImportParser getApiImportParser(String platform) {
        if (StringUtils.equals(ApiImportPlatform.Metersphere.name(), platform)) {
            return new MsParser();
        } else if (StringUtils.equals(ApiImportPlatform.Postman.name(), platform)) {
            return new ScenarioPostmanParser();
        }
        return null;
    }
}
