package io.metersphere.api.parser.api.dataimport;


import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiDefinitionImportDataAnalysisResult;
import io.metersphere.api.dto.converter.ApiDefinitionImportFileParseResult;
import io.metersphere.api.dto.converter.ExistenceApiDefinitionDetail;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.api.dto.request.http.body.WWWFormKV;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.parser.api.har.HarUtils;
import io.metersphere.api.parser.api.har.model.Har;
import io.metersphere.api.parser.api.har.model.HarEntry;
import io.metersphere.api.parser.api.har.model.HarRequest;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HarParserApiDefinition extends HttpApiDefinitionImportAbstractParser<ApiDefinitionImportFileParseResult> {

    @Override
    public ApiDefinitionImportFileParseResult parse(InputStream source, ImportRequest request) throws Exception {
        Har har = null;
        try {
            har = HarUtils.read(source);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(e.getMessage());
        }
        if (ObjectUtils.isEmpty(har) || har.log == null) {
            throw new MSException("解析失败，请确认选择的是 Har 格式！");
        }
        ApiDefinitionImportFileParseResult definitionImport = new ApiDefinitionImportFileParseResult();
        definitionImport.setData(parseRequests(har, request));
        return definitionImport;
    }

    @Override
    public ApiDefinitionImportDataAnalysisResult generateInsertAndUpdateData(ApiDefinitionImportFileParseResult importParser, List<ApiDefinitionDetail> existenceApiDefinitionList) {
        ApiDefinitionImportDataAnalysisResult insertAndUpdateData = super.generateInsertAndUpdateData(importParser, existenceApiDefinitionList);
        ApiDefinitionBlobMapper apiDefinitionBlobMapper = CommonBeanFactory.getBean(ApiDefinitionBlobMapper.class);

        for (ExistenceApiDefinitionDetail definitionDetail : insertAndUpdateData.getExistenceApiList()) {
            ApiDefinitionDetail importApi = definitionDetail.getImportApiDefinition();
            ApiDefinitionDetail savedApi = definitionDetail.getExistenceApiDefinition().getFirst();
            ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(savedApi.getId());
            if (blob != null) {
                if (blob.getRequest() != null) {
                    AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);
                    savedApi.setRequest(msTestElement);
                }
                if (blob.getResponse() != null) {
                    List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class);
                    savedApi.setResponse(httpResponses);
                }
            }
            this.mergeExistenceApiMap(importApi, savedApi);
        }
        return insertAndUpdateData;
    }

    private void mergeExistenceApiMap(ApiDefinitionDetail importApi, ApiDefinitionDetail savedApi) {
        MsHTTPElement importHttpElement = (MsHTTPElement) importApi.getRequest();
        if (savedApi.getRequest() != null) {
            MsHTTPElement existenceHttpElement = (MsHTTPElement) savedApi.getRequest();
            importHttpElement.setOtherConfig(existenceHttpElement.getOtherConfig());
            importHttpElement.setModuleId(existenceHttpElement.getModuleId());
            importHttpElement.setNum(existenceHttpElement.getNum());
            importHttpElement.setMockNum(existenceHttpElement.getMockNum());
            importHttpElement.setBody(this.mergeBody(importHttpElement.getBody(), existenceHttpElement.getBody()));
            importHttpElement.setHeaders(this.mergeHeaders(importHttpElement.getHeaders(), existenceHttpElement.getHeaders()));
            importHttpElement.setQuery(this.mergeQuery(importHttpElement.getQuery(), existenceHttpElement.getQuery()));
            importHttpElement.setRest(this.mergeRest(importHttpElement.getRest(), existenceHttpElement.getRest()));
        }
        importApi.setRequest(importHttpElement);

        if (CollectionUtils.isEmpty(importApi.getResponse())) {
            importApi.setResponse(savedApi.getResponse());
        } else {
            if (CollectionUtils.isEmpty(savedApi.getResponse())) {
                importApi.getResponse().getFirst().setDefaultFlag(true);
            } else {
                List<HttpResponse> existenceResponseList = savedApi.getResponse();
                for (HttpResponse importRsp : importApi.getResponse()) {
                    boolean isExistence = false;
                    for (HttpResponse existenceRsp : existenceResponseList) {
                        if (StringUtils.equals(importRsp.getName(), existenceRsp.getName())) {
                            isExistence = true;
                            existenceRsp.setBody(importRsp.getBody());
                            existenceRsp.setHeaders(importRsp.getHeaders());
                            existenceRsp.setStatusCode(importRsp.getStatusCode());
                        }
                    }
                    if (!isExistence) {
                        importRsp.setId(IDGenerator.nextStr());
                        existenceResponseList.add(importRsp);
                    }
                }
                if (CollectionUtils.isNotEmpty(existenceResponseList)) {
                    importApi.setResponse(existenceResponseList);
                }
            }
        }

    }

    private List<RestParam> mergeRest(List<RestParam> importRestList, List<RestParam> existenceRestList) {
        if (CollectionUtils.isNotEmpty(importRestList) && CollectionUtils.isNotEmpty(existenceRestList)) {
            for (RestParam importRest : importRestList) {
                for (RestParam existenceRest : existenceRestList) {
                    if (StringUtils.equals(importRest.getKey(), existenceRest.getKey())) {
                        importRest.setDescription(existenceRest.getDescription());
                        importRest.setMaxLength(existenceRest.getMaxLength());
                        importRest.setMinLength(existenceRest.getMinLength());
                        importRest.setEncode(existenceRest.getEncode());
                    }
                }
            }
        }
        return importRestList;
    }

    private List<QueryParam> mergeQuery(List<QueryParam> importQueryList, List<QueryParam> existenceQueryList) {
        if (CollectionUtils.isNotEmpty(importQueryList) && CollectionUtils.isNotEmpty(existenceQueryList)) {
            for (QueryParam importQuery : importQueryList) {
                for (QueryParam existenceQuery : existenceQueryList) {
                    if (StringUtils.equals(importQuery.getKey(), existenceQuery.getKey())) {
                        importQuery.setDescription(existenceQuery.getDescription());
                        importQuery.setMaxLength(existenceQuery.getMaxLength());
                        importQuery.setMinLength(existenceQuery.getMinLength());
                        importQuery.setEncode(existenceQuery.getEncode());
                    }
                }
            }
        }
        return importQueryList;
    }

    private List<MsHeader> mergeHeaders(List<MsHeader> importHeaders, List<MsHeader> existenceHeaders) {
        if (CollectionUtils.isNotEmpty(importHeaders) && CollectionUtils.isNotEmpty(existenceHeaders)) {
            for (MsHeader importHeader : importHeaders) {
                for (MsHeader existenceHeader : existenceHeaders) {
                    if (StringUtils.equals(importHeader.getKey(), existenceHeader.getKey())) {
                        importHeader.setDescription(existenceHeader.getDescription());
                    }
                }
            }
        }
        return importHeaders;
    }

    private Body mergeBody(Body importBody, Body existenceBody) {
        if (importBody == null) {
            return existenceBody;
        } else if (existenceBody == null) {
            return importBody;
        } else {
            Body returnBody = new Body();
            BeanUtils.copyBean(returnBody, existenceBody);

            if (importBody.getBinaryBody() != null) {
                returnBody.setBinaryBody(importBody.getBinaryBody());
            }
            if (importBody.getFormDataBody() != null) {
                if (returnBody.getFormDataBody() != null) {
                    for (FormDataKV importKv : importBody.getFormDataBody().getFormValues()) {
                        for (FormDataKV existenceKv : returnBody.getFormDataBody().getFormValues()) {
                            if (StringUtils.equals(existenceKv.getKey(), importKv.getKey())) {
                                importKv.setDescription(existenceKv.getDescription());
                                importKv.setMaxLength(existenceKv.getMaxLength());
                                importKv.setMinLength(existenceKv.getMinLength());
                            }
                        }
                    }
                }
                returnBody.setFormDataBody(importBody.getFormDataBody());
            }
            if (importBody.getJsonBody() != null) {
                if (returnBody.getJsonBody() != null) {
                    returnBody.getJsonBody().setJsonValue(importBody.getJsonBody().getJsonValue());
                } else {
                    returnBody.setJsonBody(importBody.getJsonBody());
                }
            }
            if (importBody.getNoneBody() != null) {
                returnBody.setNoneBody(importBody.getNoneBody());
            }
            if (importBody.getRawBody() != null) {
                returnBody.setRawBody(importBody.getRawBody());
            }
            if (importBody.getWwwFormBody() != null) {
                if (returnBody.getWwwFormBody() != null) {
                    for (WWWFormKV importKv : importBody.getWwwFormBody().getFormValues()) {
                        for (WWWFormKV existenceKv : returnBody.getWwwFormBody().getFormValues()) {
                            if (StringUtils.equals(existenceKv.getKey(), importKv.getKey())) {
                                importKv.setDescription(existenceKv.getDescription());
                                importKv.setMaxLength(existenceKv.getMaxLength());
                                importKv.setMinLength(existenceKv.getMinLength());
                            }
                        }
                    }
                }
                returnBody.setWwwFormBody(importBody.getWwwFormBody());
            }
            if (importBody.getXmlBody() != null) {
                returnBody.setXmlBody(importBody.getXmlBody());
            }
            return returnBody;
        }
    }

    private List<ApiDefinitionDetail> parseRequests(Har har, ImportRequest importRequest) {
        List<ApiDefinitionDetail> resultList = new ArrayList<>();

        List<HarEntry> harEntryList = new ArrayList<>();
        if (har.log != null && har.log.entries != null) {
            harEntryList = har.log.entries;
        }

        for (HarEntry entry : harEntryList) {
            HarRequest harRequest = entry.request;
            if (harRequest != null) {
                // css 、 js 略过
                if (StringUtils.equalsIgnoreCase(harRequest.method, HttpMethodConstants.GET.name()) && StringUtils.endsWithAny(harRequest.url.toLowerCase(), ".css", ".js", ".png", ".jpg", ".jpeg")) {
                    continue;
                }
                String url = harRequest.url;
                if (url == null) {
                    continue;
                }
                //默认取路径的最后一块
                String[] nameArr = url.split("/");
                String reqName = nameArr[nameArr.length - 1];
                try {
                    url = URLDecoder.decode(url, StandardCharsets.UTF_8);
                    if (url.contains("?")) {
                        url = url.split("\\?")[0];
                    }
                } catch (Exception e) {
                    LogUtils.error(e.getMessage(), e);
                }


                ApiDefinitionDetail detail = buildApiDefinition(reqName, url, harRequest.method, null, importRequest);
                MsHTTPElement request = super.buildHttpRequest(reqName, url, harRequest.method);
                HarUtils.parseParameters(harRequest, request);
                HarUtils.parseRequestBody(harRequest, request.getBody());
                detail.setRequest(request);
                detail.setResponse(Collections.singletonList(HarUtils.parseResponse(entry.response)));
                resultList.add(detail);
            }
        }

        return resultList;
    }

}
