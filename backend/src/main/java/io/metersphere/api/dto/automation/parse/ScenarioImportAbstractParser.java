package io.metersphere.api.dto.automation.parse;

import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.ApiScenarioModuleService;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ScenarioImportAbstractParser implements ScenarioImportParser {
    protected ApiScenarioModuleService apiModuleService;
    protected String projectId;

    protected ApiScenarioModule getSelectModule(String moduleId) {
        apiModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
        if (StringUtils.isNotBlank(moduleId) && !StringUtils.equals("root", moduleId)) {
            ApiScenarioModule module = new ApiScenarioModule();
            ApiScenarioModuleDTO moduleDTO = apiModuleService.getNode(moduleId);
            if (moduleDTO != null) {
                BeanUtils.copyBean(module, moduleDTO);
            }
            return module;
        }
        return null;
    }

    protected ApiScenarioModule buildModule(ApiScenarioModule parentModule, String name) {
        apiModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
        ApiScenarioModule module;
        if (parentModule != null) {
            module = apiModuleService.getNewModule(name, this.projectId, parentModule.getLevel() + 1);
            module.setParentId(parentModule.getId());
        } else {
            module = apiModuleService.getNewModule(name, this.projectId, 1);
        }
        createModule(module);
        return module;
    }

    protected void createModule(ApiScenarioModule module) {
        List<ApiScenarioModule> apiModules = apiModuleService.selectSameModule(module);
        if (CollectionUtils.isEmpty(apiModules)) {
            apiModuleService.addNode(module);
        } else {
            module.setId(apiModules.get(0).getId());
        }
    }

    protected String getApiTestStr(InputStream source) {
        StringBuilder testStr = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8))) {
            testStr = new StringBuilder();
            String inputStr;
            while ((inputStr = bufferedReader.readLine()) != null) {
                testStr.append(inputStr);
            }
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
            } catch (IOException e) {
                MSException.throwException(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        }
        return testStr.toString();
    }

    private String formatPath(String url) {
        try {
            URL urlObject = new URL(url);
            StringBuilder pathBuffer = new StringBuilder(urlObject.getPath());
            if (StringUtils.isNotEmpty(urlObject.getQuery())) {
                pathBuffer.append("?").append(urlObject.getQuery());
            }
            return pathBuffer.toString();
        } catch (Exception ex) {
            return url;
        }
    }

    protected MsHTTPSamplerProxy buildRequest(String name, String path, String method) {
        MsHTTPSamplerProxy request = new MsHTTPSamplerProxy();
        request.setName(name);
        // 路径去掉域名/IP 地址，保留方法名称及参数
        request.setPath(formatPath(path));
        request.setMethod(method);
        request.setProtocol(RequestType.HTTP);
        request.setId(UUID.randomUUID().toString());
        request.setHeaders(new ArrayList<>());
        request.setArguments(new ArrayList<>());
        request.setRest(new ArrayList<>());
        request.setAuthManager(null);
        Body body = new Body();
        body.initKvs();
        body.initBinary();
        request.setBody(body);
        return request;
    }

    protected void addHeader(List<KeyValue> headers, String key, String value, String description, String contentType, boolean required) {
        boolean hasContentType = false;
        for (KeyValue header : headers) {
            if (StringUtils.equalsIgnoreCase(header.getName(), key)) {
                hasContentType = true;
            }
        }
        if (!hasContentType) {
            headers.add(new KeyValue(key, value, description, contentType, required));
        }
    }

    protected void addHeader(List<KeyValue> headers, String key, String value) {
        addHeader(headers, key, value, "", "", true);
    }

    protected void addContentType(List<KeyValue> headers, String contentType) {
        addHeader(headers, "Content-Type", contentType);
    }

    protected void addBodyHeader(MsHTTPSamplerProxy request) {
        String contentType = "";
        if (request.getBody() != null && StringUtils.isNotBlank(request.getBody().getType())) {
            switch (request.getBody().getType()) {
                case Body.JSON:
                    contentType = "application/json";
                    break;
                case Body.WWW_FROM:
                    contentType = "application/x-www-form-urlencoded";
                    break;
                case Body.XML:
                    contentType = "application/xml";
                    break;
                case Body.BINARY:
                    contentType = "application/octet-stream";
                    break;
            }
            List<KeyValue> headers = request.getHeaders();
            if (headers == null) {
                headers = new ArrayList<>();
                request.setHeaders(headers);
            }
            addContentType(request.getHeaders(), contentType);

        }
    }

}
