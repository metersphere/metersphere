package io.metersphere.api.parser.step;

import io.metersphere.api.domain.ApiTestCaseBlob;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepDetailRequest;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-20  15:43
 */
public class ApiCaseStepParser extends StepParser {
    @Override
    public AbstractMsTestElement parseTestElement(ApiScenarioStepCommonDTO step, String resourceBlob, String stepDetail) {
        if (isRef(step.getRefType())) {
            return StringUtils.isBlank(resourceBlob) ? null : parse2MsTestElement(resourceBlob);
        } else {
            if (StringUtils.isBlank(stepDetail)) {
                return null;
            }
            return StringUtils.isBlank(stepDetail) ? null : ApiDataUtils.parseObject(stepDetail, AbstractMsTestElement.class);
        }
    }

    @Override
    public Object parseDetail(ApiScenarioStepDetailRequest step) {
        if (isRef(step.getRefType())) {
            ApiTestCaseBlobMapper apiTestCaseBlobMapper = CommonBeanFactory.getBean(ApiTestCaseBlobMapper.class);
            ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(step.getResourceId());
            if (apiTestCaseBlob == null) {
                return null;
            }
            return parse2MsTestElement(new String(apiTestCaseBlob.getRequest()));
        } else {
            return parse2MsTestElement(getStepBlobString(step.getId()));
        }
    }
}
