package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.api.parser.jmeter.constants.JmeterProperty;
import io.metersphere.api.parser.jmeter.processor.ScriptProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.MsScriptAssertion;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.sdk.dto.api.result.ResponseAssertionResult;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.BeanShellAssertion;
import org.apache.jmeter.assertions.JSR223Assertion;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.Optional;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  21:01
 */
public class ScriptAssertionConverter extends AssertionConverter<MsScriptAssertion> {
    @Override
    public void parse(HashTree hashTree, MsScriptAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus) {
        if (!needParse(msAssertion, config) || !msAssertion.isValid()) {
            return;
        }

        AbstractTestElement assertion;
        if (isJSR233(msAssertion)) {
            assertion = new JSR223Assertion();
        } else {
            assertion = new BeanShellAssertion();
        }
        ScriptProcessor scriptProcessor = BeanUtils.copyBean(new ScriptProcessor(), msAssertion);
        ScriptProcessorConverter.parse(assertion, scriptProcessor, config);

        if (!isJSR233(msAssertion)) {
            // beanshell 断言参数名有区别，替换一下
            assertion.setProperty(JmeterProperty.BEAN_SHELL_ASSERTION_QUERY, assertion.getProperty(JmeterProperty.SCRIPT).getStringValue());
            assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JmeterAlias.BEAN_SHELL_ASSERTION_GUI));
        }

        // 添加公共脚本的参数
        Optional.ofNullable(ScriptProcessorConverter.getScriptArguments(scriptProcessor))
                .ifPresent(hashTree::add);

        setMsAssertionInfoProperty(assertion, ResponseAssertionResult.AssertionResultType.SCRIPT.name(), assertion.getName());

        hashTree.add(assertion);
    }

    public static boolean isJSR233(MsScriptAssertion msScriptAssertion) {
        return !StringUtils.equals(msScriptAssertion.getScriptLanguage(), ScriptLanguageType.BEANSHELL.name());
    }
}
