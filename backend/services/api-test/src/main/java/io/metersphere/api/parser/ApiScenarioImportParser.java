package io.metersphere.api.parser;


import io.metersphere.api.dto.scenario.ApiScenarioImportDetail;
import io.metersphere.api.dto.scenario.ApiScenarioImportRequest;

import java.io.InputStream;
import java.util.List;

public interface ApiScenarioImportParser {

    /**
     * 解析导入文件
     *
     * @param source  导入文件流
     * @param request 导入的请求参数
     * @return 解析后的数据
     */
    List<ApiScenarioImportDetail> parse(InputStream source, ApiScenarioImportRequest request) throws Exception;

}
