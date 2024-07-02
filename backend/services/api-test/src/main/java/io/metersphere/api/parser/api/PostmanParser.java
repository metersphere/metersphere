package io.metersphere.api.parser.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.metersphere.api.dto.converter.ApiDefinitionImport;
import io.metersphere.api.dto.converter.ApiDefinitionImportDetail;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.parser.api.postman.PostmanCollection;
import io.metersphere.api.parser.api.postman.PostmanItem;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PostmanParser extends PostmanAbstractParserParser<ApiDefinitionImport> {

    @Override
    public ApiDefinitionImport parse(InputStream source, ImportRequest request) throws JsonProcessingException {
        LogUtils.info("PostmanParser parse");
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        if (postmanCollection == null || postmanCollection.getItem() == null || postmanCollection.getItem().isEmpty() || postmanCollection.getInfo() == null){
            throw new MSException("Postman collection is empty");
        }
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionImportDetail> results = new ArrayList<>();

        String modulePath = null;
        if (StringUtils.isNotBlank(postmanCollection.getInfo().getName())) {
            modulePath = "/" + postmanCollection.getInfo().getName();
        }
        parseItem(postmanCollection.getItem(), modulePath, results, request);
        apiImport.setData(results);
        //apiImport.setCases(cases);
        LogUtils.info("PostmanParser parse end");
        return apiImport;
    }

    protected void parseItem(List<PostmanItem> items, String modulePath, List<ApiDefinitionImportDetail> results, ImportRequest request) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                String itemModulePath;
                if (StringUtils.isNotBlank(modulePath) && StringUtils.isNotBlank(item.getName())) {
                    itemModulePath = modulePath + "/" + item.getName();
                } else {
                    itemModulePath = item.getName();
                }
                parseItem(childItems, itemModulePath, results, request);
            } else {
                results.add(parsePostman(item, modulePath, request));
            }
        }
    }
}
