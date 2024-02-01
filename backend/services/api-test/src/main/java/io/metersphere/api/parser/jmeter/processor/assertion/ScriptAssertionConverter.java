package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.project.api.assertion.MsScriptAssertion;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.api.parser.jmeter.processor.ScriptProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.JSR223Assertion;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  21:01
 */
public class ScriptAssertionConverter extends AssertionConverter<MsScriptAssertion> {
    @Override
    public void parse(HashTree hashTree, MsScriptAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus) {
        if (!needParse(msAssertion, config) || !isValid(msAssertion)) {
            return;
        }

        JSR223Assertion jsr223Assertion = new JSR223Assertion();
        ScriptProcessorConverter.parse(jsr223Assertion, BeanUtils.copyBean(new ScriptProcessor(), msAssertion));
        hashTree.add(jsr223Assertion);
    }

    public boolean isValid(MsScriptAssertion msAssertion) {
        // todo 公共脚本库
        return StringUtils.isNotBlank(msAssertion.getScript());
    }
}
