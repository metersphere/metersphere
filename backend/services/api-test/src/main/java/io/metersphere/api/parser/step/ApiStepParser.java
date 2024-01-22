package io.metersphere.api.parser.step;

import io.metersphere.api.dto.request.http.KeyValueParam;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public class ApiStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parse(ApiScenarioStepRequest step, String resourceBlob, String stepDetail) {
        if (isRef(step.getRefType())) {
            if (StringUtils.isBlank(resourceBlob)) {
                return null;
            }
            AbstractMsTestElement refResourceElement = parse2MsTestElement(resourceBlob);
            if (refResourceElement instanceof MsHTTPElement && StringUtils.isNotBlank(stepDetail)) {
                // 如果是 http 并且有修改请求参数，则替换请求参数
                AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(stepDetail, AbstractMsTestElement.class);
                return replaceParams((MsHTTPElement) msTestElement, (MsHTTPElement) refResourceElement);
            } else {
                return refResourceElement;
            }
        } else {
            return StringUtils.isBlank(stepDetail) ? null : ApiDataUtils.parseObject(stepDetail, AbstractMsTestElement.class);
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
        if (StringUtils.equals(refBody.getBodyType(), Body.BodyType.FORM_DATA.name()) &&
                valueBody.getFormDataBody() != null && refBody.getFormDataBody() != null) {
            replaceKvParam(valueBody.getFormDataBody().getFromValues(), valueBody.getFormDataBody().getFromValues());
        }
        if (StringUtils.equals(refBody.getBodyType(), Body.BodyType.WWW_FORM.name()) &&
                valueBody.getWwwFormBody() != null && refBody.getWwwFormBody() != null) {
            replaceKvParam(valueBody.getWwwFormBody().getFromValues(), valueBody.getWwwFormBody().getFromValues());
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
            KeyValueParam keyValueParam = (KeyValueParam) item;
            for (Object valueItem : valueList) {
                KeyValueParam valueParam = (KeyValueParam) valueItem;
                if (StringUtils.equals(keyValueParam.getKey(), valueParam.getKey())) {
                    keyValueParam.setValue(valueParam.getValue());
                    break;
                }
            }
        });
    }
}
