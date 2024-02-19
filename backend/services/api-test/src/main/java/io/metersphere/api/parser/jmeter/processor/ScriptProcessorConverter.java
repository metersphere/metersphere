package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.plugin.api.constants.ElementProperty;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.api.parser.jmeter.constants.JmeterProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;


/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public abstract class ScriptProcessorConverter extends MsProcessorConverter<ScriptProcessor> {

    public static final String ENV_VARIABLE_EXPRESSION = "${__metersphere_env_id}";
    public static final String MS_RUNNING_ENV_PREFIX = "MS.ENV.";

    public static void parse(TestElement testElement, ScriptProcessor scriptProcessor) {
        // 脚本安全校验
        ScriptFilter.verify(scriptProcessor.getScriptLanguage(), scriptProcessor.getName(), scriptProcessor.getScript());

        testElement.setEnabled(scriptProcessor.getEnable());
        String name = StringUtils.isEmpty(scriptProcessor.getName()) ? scriptProcessor.getClass().getSimpleName() : scriptProcessor.getName();
        testElement.setName(name);

        // todo 替换环境变量
//        String evnId = scriptProcessor.getEnvironmentId();
//        if (StringUtils.isNotEmpty(scriptProcessor.getScript())) {
//            scriptProcessor.setScript(StringUtils.replace(scriptProcessor.getScript(), ENV_VARIABLE_EXPRESSION, "\"" + MS_RUNNING_ENV_PREFIX + evnId + ".\""));
//        }

        // python 和 js cache 打开
        boolean cacheKey = StringUtils.equalsAny(scriptProcessor.getScriptLanguage(), ScriptLanguageType.PYTHON.getValue(), ScriptLanguageType.JAVASCRIPT.getValue());
        testElement.setProperty(JmeterProperty.CACHE_KEY, cacheKey);
        testElement.setProperty(TestElement.TEST_CLASS, testElement.getClass().getSimpleName());
        testElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JmeterAlias.TEST_BEAN_GUI));
        testElement.setProperty(JmeterProperty.SCRIPT_LANGUAGE, scriptProcessor.getScriptLanguage());
        testElement.setProperty(JmeterProperty.SCRIPT, scriptProcessor.getScript());
        testElement.setProperty(ElementProperty.PROJECT_ID.name(), scriptProcessor.getProjectId());
    }

    public static boolean isJSR233(ScriptProcessor scriptProcessor) {
        return !StringUtils.equals(scriptProcessor.getScriptLanguage(), ScriptLanguageType.BEANSHELL.getValue());
    }
}
