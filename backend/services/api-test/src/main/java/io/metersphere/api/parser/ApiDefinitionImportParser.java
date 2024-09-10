package io.metersphere.api.parser;


import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiDefinitionImportDataAnalysisResult;
import io.metersphere.api.dto.converter.ApiDefinitionImportFileParseResult;
import io.metersphere.api.dto.request.ImportRequest;

import java.io.InputStream;
import java.util.List;

public interface ApiDefinitionImportParser<T> {

    /**
     * 解析导入文件
     *
     * @param source  导入文件流
     * @param request 导入的请求参数
     * @return 解析后的数据
     * @throws Exception
     */
    T parse(InputStream source, ImportRequest request) throws Exception;

    /**
     * 判断当前导入的接口定义中，哪些是数据库中存在（需要更新）的，哪些是不存在（需要插入）的。
     *
     * @param importParser 准备导入的数据
     * @param existenceApiDefinitionList    数据库中已存在的数据
     * @return 需要入库的模块、需要入库的接口、需要更新的接口
     */
    ApiDefinitionImportDataAnalysisResult generateInsertAndUpdateData(ApiDefinitionImportFileParseResult importParser, List<ApiDefinitionDetail> existenceApiDefinitionList);

}
