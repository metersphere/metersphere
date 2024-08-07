package io.metersphere.api.parser.jmeter.processor.assertion.body;

import io.metersphere.api.parser.jmeter.processor.assertion.AssertionConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.body.MsBodyAssertionItem;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-02  21:40
 */
public abstract class ResponseBodyTypeAssertionConverter <T> {
    /**
     * 解析对应的提取器
     *
     * @param hashTree
     * @param extract
     * @param config
     */
    public abstract void parse(HashTree hashTree, T extract, ParameterConfig config, boolean isIgnoreStatus, boolean globalEnable);

    protected boolean needParse(MsBodyAssertionItem msAssertion, ParameterConfig config) {
        // 如果组件是启用的，或者设置了解析禁用的组件，则返回 true
        if (BooleanUtils.isTrue(msAssertion.getEnable()) || config.getParseDisabledElement()) {
            return true;
        }
        return false;
    }

    public void setMsAssertionInfoProperty(AbstractTestElement assertion, String assertionType, String name, String condition, String expectedValue) {
        AssertionConverter.setMsAssertionInfoProperty(assertion, assertionType, name, condition, expectedValue);
    }

    public void setMsAssertionInfoProperty(AbstractTestElement assertion, String assertionType, String name) {
        AssertionConverter.setMsAssertionInfoProperty(assertion, assertionType, name);
    }
}