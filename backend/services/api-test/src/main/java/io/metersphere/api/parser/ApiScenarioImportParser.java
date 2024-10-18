package io.metersphere.api.parser;


import io.metersphere.api.dto.converter.ApiScenarioImportParseResult;
import io.metersphere.api.dto.scenario.ApiScenarioImportRequest;

import java.io.InputStream;

public interface ApiScenarioImportParser {

    /**
     * 解析导入文件
     *
     * @param source  导入文件流
     * @param request 导入的请求参数
     * @return 解析后的数据
     */
    ApiScenarioImportParseResult parse(InputStream source, ApiScenarioImportRequest request) throws Exception;

}
