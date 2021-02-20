package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MsScenarioParser extends ScenarioImportAbstractParser {

    @Override
    public ScenarioImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        ScenarioImport scenarioImport = parseMsFormat(testStr, request);
        return scenarioImport;
    }

    private ScenarioImport parseMsFormat(String testStr, ApiTestImportRequest importRequest) {
        ScenarioImport apiDefinitionImport = JSON.parseObject(testStr, ScenarioImport.class);
        List<ApiScenarioWithBLOBs> data = apiDefinitionImport.getData();
        if (CollectionUtils.isNotEmpty(data)) {
            data.forEach(item -> {
                if (StringUtils.isBlank(item.getModulePath())) {
                    item.setApiScenarioModuleId(null);
                }
                parseModule(item, importRequest);
                item.setId(UUID.randomUUID().toString());
                item.setProjectId(this.projectId);
            });
        }
        return apiDefinitionImport;
    }

    private void parseModule(ApiScenarioWithBLOBs apiDefinition, ApiTestImportRequest importRequest) {
        String modulePath = apiDefinition.getModulePath();
        if (StringUtils.isEmpty(modulePath)) {
            return;
        }
        if (modulePath.startsWith("/")) {
            modulePath = modulePath.substring(1, modulePath.length());
        }
        if (modulePath.endsWith("/")) {
            modulePath = modulePath.substring(0, modulePath.length() - 1);
        }
        List<String> modules = Arrays.asList(modulePath.split("/"));
        ApiScenarioModule parent = getSelectModule(importRequest.getModuleId());
        Iterator<String> iterator = modules.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            parent = buildModule(parent, item);
            if (!iterator.hasNext()) {
                apiDefinition.setApiScenarioModuleId(parent.getId());
            }
        }
    }

}
