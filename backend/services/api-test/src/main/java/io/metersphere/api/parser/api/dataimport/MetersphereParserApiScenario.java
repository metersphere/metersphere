package io.metersphere.api.parser.api.dataimport;


import io.metersphere.api.dto.scenario.ApiScenarioImportDetail;
import io.metersphere.api.dto.scenario.ApiScenarioImportRequest;
import io.metersphere.api.parser.ApiScenarioImportParser;

import java.io.InputStream;
import java.util.List;

public class MetersphereParserApiScenario implements ApiScenarioImportParser {

    @Override
    public List<ApiScenarioImportDetail> parse(InputStream source, ApiScenarioImportRequest request) throws Exception {
        return null;
        //        MetersphereApiExportResponse metersphereApiExportResponse = null;
        //        try {
        //            metersphereApiExportResponse = ApiDataUtils.parseObject(source, MetersphereApiExportResponse.class);
        //        } catch (Exception e) {
        //            LogUtils.error(e.getMessage(), e);
        //            throw new MSException(e.getMessage());
        //        }
        //        if (metersphereApiExportResponse == null) {
        //            throw new MSException("解析失败，请确认选择的是 Metersphere 格式！");
        //        }
        //        return this.genApiDefinitionImport(metersphereApiExportResponse.getApiDefinitions());
    }

}
