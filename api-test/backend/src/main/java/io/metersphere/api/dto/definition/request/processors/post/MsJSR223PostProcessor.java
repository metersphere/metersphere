package io.metersphere.api.dto.definition.request.processors.post;

import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.shell.filter.ScriptFilter;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsJSR223PostProcessor extends MsTestElement {
    private String type = ElementConstants.JSR223_POST;
    private String clazzName = MsJSR223PostProcessor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ScriptFilter.verify(this.getScriptLanguage(), this.getName(), script);
        ParameterConfig config = (ParameterConfig) msParameter;
        if (StringUtils.isEmpty(this.getEnvironmentId())) {
            if (config.getConfig() != null) {
                if (config.getProjectId() != null && config.getConfig().containsKey(config.getProjectId())) {
                    String evnId = config.getConfig().get(config.getProjectId()).getEnvironmentId();
                    this.setEnvironmentId(evnId);
                } else {
                    Collection<EnvironmentConfig> evnConfigList = config.getConfig().values();
                    if (evnConfigList != null && !evnConfigList.isEmpty()) {
                        for (EnvironmentConfig configItem : evnConfigList) {
                            String evnId = configItem.getEnvironmentId();
                            this.setEnvironmentId(evnId);
                            break;
                        }
                    }
                }
            }
        }
        script = StringUtils.replace(script, RunningParamKeys.API_ENVIRONMENT_ID, "\"" + RunningParamKeys.RUNNING_PARAMS_PREFIX + this.getEnvironmentId() + ".\"");
        if (config.isOperating()) {
            if (StringUtils.isNotEmpty(script) && script.startsWith("io.metersphere.utils.JMeterVars.addVars")) {
                return;
            }
        }
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        JSR223PostProcessor processor = new JSR223PostProcessor();
        processor.setEnabled(this.isEnable());
        if (StringUtils.isNotEmpty(this.getName())) {
            processor.setName(this.getName());
        } else {
            processor.setName(ElementConstants.JSR223_POST);
        }
        processor.setProperty(TestElement.TEST_CLASS, JSR223PostProcessor.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        processor.setProperty("scriptLanguage", this.getScriptLanguage());
        if (StringUtils.isNotEmpty(this.getScriptLanguage()) && this.getScriptLanguage().equals("nashornScript")) {
            processor.setProperty("scriptLanguage", "nashorn");
        }
        if (StringUtils.isNotEmpty(this.getScriptLanguage()) && this.getScriptLanguage().equals("rhinoScript")) {
            processor.setProperty("scriptLanguage", "rhino");
        }
        if (StringUtils.isNotEmpty(this.getScriptLanguage()) && this.getScriptLanguage().equals("javascript")) {
            processor.setProperty("scriptLanguage", "rhino");
        }
        processor.setProperty("script", this.getScript());
        final HashTree jsr223PostTree = tree.add(processor);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(jsr223PostTree, el.getHashTree(), config);
            });
        }
    }
}
