package io.metersphere.api.dto.automation.parse;

import io.metersphere.commons.constants.ApiImportPlatform;
import org.apache.commons.lang3.StringUtils;

public class ScenarioImportParserFactory {
    public static ScenarioImportParser getImportParser(String platform) {
        if (StringUtils.equals(ApiImportPlatform.Metersphere.name(), platform)) {
            return new MsScenarioParser();
        } else if (StringUtils.equals(ApiImportPlatform.Postman.name(), platform)) {
            return new MsPostmanParser();
        }
//        else if (StringUtils.equals(ApiImportPlatform.Swagger2.name(), platform)) {
//            return new Swagger2Parser();
//        }
        return null;
    }
}
