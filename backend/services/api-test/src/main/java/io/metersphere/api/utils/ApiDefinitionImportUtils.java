package io.metersphere.api.utils;

import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPConfig;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.auth.NoAuth;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.dto.LogDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ApiDefinitionImportUtils {
    private static final String FILE_JMX = "jmx";
    private static final String FILE_HAR = "har";
    private static final String FILE_JSON = "json";

    public static MsHTTPElement buildHttpRequest(String name, String path, String method) {
        MsHTTPElement request = new MsHTTPElement();
        request.setName(name);
        // 路径去掉域名/IP 地址，保留方法名称及参数
        request.setPath(formatPath(path));
        request.setMethod(method);
        request.setHeaders(new ArrayList<>());
        request.setQuery(new ArrayList<>());
        request.setRest(new ArrayList<>());
        request.setBody(new Body());
        MsHTTPConfig httpConfig = new MsHTTPConfig();
        httpConfig.setConnectTimeout(60000L);
        httpConfig.setResponseTimeout(60000L);
        request.setOtherConfig(httpConfig);
        request.setAuthConfig(new NoAuth());
        //        assertionConfig
        Body body = new Body();
        body.setBinaryBody(new BinaryBody());
        body.setFormDataBody(new FormDataBody());
        body.setXmlBody(new XmlBody());
        body.setRawBody(new RawBody());
        body.setNoneBody(new NoneBody());
        body.setJsonBody(new JsonBody());
        body.setWwwFormBody(new WWWFormBody());
        body.setNoneBody(new NoneBody());
        body.setBodyType(Body.BodyType.NONE.name());
        request.setBody(body);

        MsCommonElement commonElement = new MsCommonElement();
        LinkedList<AbstractMsTestElement> children = new LinkedList<>();
        children.add(commonElement);
        request.setChildren(children);

        return request;
    }

    public static String formatPath(String url) {
        try {
            URI urlObject = new URI(url);
            return StringUtils.isBlank(urlObject.getPath()) ? url : urlObject.getPath();
        } catch (Exception ex) {
            //只需要返回？前的路径
            return url.split("\\?")[0];
        }
    }

    public static void checkFileSuffixName(ImportRequest request, String suffixName) {
        if (FILE_JMX.equalsIgnoreCase(suffixName)) {
            if (!ApiImportPlatform.Jmeter.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (FILE_HAR.equalsIgnoreCase(suffixName)) {
            if (!ApiImportPlatform.Har.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (FILE_JSON.equalsIgnoreCase(suffixName)) {
            if (ApiImportPlatform.Har.name().equalsIgnoreCase(request.getPlatform()) || ApiImportPlatform.Jmeter.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
    }

    public static LogDTO genImportLog(Project project, String dataId, String dataName, Object importData, String module, String operator, String operationType) {
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                dataId,
                operator,
                operationType,
                module,
                dataName);
        dto.setHistory(true);
        dto.setPath("/api/definition/import");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(importData));
        return dto;
    }

    public static List<ApiDefinitionDetail> apiRename(List<ApiDefinitionDetail> caseList) {
        List<ApiDefinitionDetail> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiDefinitionDetail apiCase : caseList) {
                String uniqueName = getUniqueName(apiCase.getName(), caseNameList);
                apiCase.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiCase);
            }
        }
        return returnList;
    }

    public static List<ApiTestCaseDTO> apiCaseRename(List<ApiTestCaseDTO> caseList) {
        List<ApiTestCaseDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiTestCaseDTO apiCase : caseList) {
                String uniqueName = getUniqueName(apiCase.getName(), caseNameList);
                apiCase.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiCase);
            }
        }
        return returnList;
    }

    public static List<ApiDefinitionMockDTO> apiMockRename(List<ApiDefinitionMockDTO> caseList) {
        List<ApiDefinitionMockDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiDefinitionMockDTO apiMock : caseList) {
                String uniqueName = ApiDefinitionImportUtils.getUniqueName(apiMock.getName(), caseNameList);
                apiMock.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiMock);
            }
        }
        return returnList;
    }

    public static String getUniqueName(String originalName, List<String> existenceNameList) {
        String returnName = originalName;
        if (existenceNameList.contains(returnName)) {
            if (originalName.length() > 250) {
                originalName = originalName.trim().substring(0, 250);
            }
            int index = 1;
            do {
                returnName = originalName + "-" + index;
                index++;
            } while (existenceNameList.contains(returnName));
        }
        return returnName;
    }
}
