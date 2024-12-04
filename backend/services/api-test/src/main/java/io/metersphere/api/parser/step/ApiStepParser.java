package io.metersphere.api.parser.step;

import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepDetailRequest;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.sdk.util.CommonBeanFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public class ApiStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        if (isRef(step.getRefType())) {
            return parseRefTestElement(resourceBlob, stepDetail);
        } else {
            return StringUtils.isBlank(stepDetail) ? null : ApiDataUtils.parseObject(stepDetail, AbstractMsTestElement.class);
        }
    }

    /**
     * 处理引用的接口步骤
     * 替换修改的参数
     * @param resourceBlob 引用的接口步骤详情
     * @param stepDetail 引用之后修改的步骤详情
     * @return
     */
    public AbstractMsTestElement parseRefTestElement(String resourceBlob, String stepDetail) {
        if (StringUtils.isBlank(resourceBlob)) {
            return null;
        }
        AbstractMsTestElement refResourceElement = parse2MsTestElement(resourceBlob);
        if (refResourceElement instanceof MsHTTPElement && StringUtils.isNotBlank(stepDetail)) {
            // 如果是 http 并且有修改请求参数，则替换请求参数
            AbstractMsTestElement stepElement = parse2MsTestElement(stepDetail);
            return replaceParams((MsHTTPElement) stepElement, (MsHTTPElement) refResourceElement);
        } else {
            return refResourceElement;
        }
    }

    @Override
    public Object parseDetail(ApiScenarioStepDetailRequest step) {
        if (isRef(step.getRefType())) {
            ApiDefinitionBlobMapper apiDefinitionBlobMapper = CommonBeanFactory.getBean(ApiDefinitionBlobMapper.class);
            ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(step.getResourceId());
            if (apiDefinitionBlob == null) {
                return null;
            }
            return parseRefTestElement(new String(apiDefinitionBlob.getRequest()), getStepBlobString(step.getId()));
        } else {
            return parse2MsTestElement(getStepBlobString(step.getId()));
        }
    }


    private AbstractMsTestElement replaceParams(MsHTTPElement msTestElement, MsHTTPElement refResourceElement) {
        replaceKvParam(msTestElement.getHeaders(), refResourceElement.getHeaders());
        replaceKvParam(msTestElement.getQuery(), refResourceElement.getQuery());
        replaceKvParam(msTestElement.getRest(), refResourceElement.getRest());
        replaceBodyParams(msTestElement.getBody(), refResourceElement.getBody());
        return refResourceElement;
    }

    /**
     * 替换请求体中的参数
     *
     * @param valueBody
     * @param refBody
     */
    private void replaceBodyParams(Body valueBody, Body refBody) {
        if (refBody == null || valueBody == null) {
            return;
        }
        if (valueBody.getFormDataBody() != null && refBody.getFormDataBody() != null) {
            replaceKvParam(valueBody.getFormDataBody().getFormValues(), refBody.getFormDataBody().getFormValues());
        }
        if (valueBody.getWwwFormBody() != null && refBody.getWwwFormBody() != null) {
            replaceKvParam(valueBody.getWwwFormBody().getFormValues(), refBody.getWwwFormBody().getFormValues());
        }
        if (valueBody.getBinaryBody() != null && refBody.getBinaryBody() != null) {
            refBody.getBinaryBody().setFile(valueBody.getBinaryBody().getFile());
        }
        // todo JsonSchema body
    }

    /**
     * 替换参数
     *
     * @param valueList
     * @param refList
     */
    private void replaceKvParam(List valueList, List refList) {
        if (CollectionUtils.isEmpty(refList) || CollectionUtils.isEmpty(valueList)) {
            return;
        }
        refList.forEach(item -> {
            KeyValueParam refParam = (KeyValueParam) item;
            for (Object valueItem : valueList) {
                KeyValueParam valueParam = (KeyValueParam) valueItem;
                if (StringUtils.equals(refParam.getKey(), valueParam.getKey())) {
                    refParam.setValue(valueParam.getValue());
                    if (refParam instanceof FormDataKV refFormDataKey && valueParam instanceof FormDataKV valueFormDataKey) {
                        refFormDataKey.setFiles(valueFormDataKey.getFiles());
                    }
                    break;
                }
            }
        });
    }
}
