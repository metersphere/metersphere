package io.metersphere.api.dto.definition.request.processors.post;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
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
@JSONType(typeName = "JSR223PostProcessor")
public class MsJSR223PostProcessor extends MsTestElement {
    private String type = "JSR223PostProcessor";

    @JSONField(ordinal = 20)
    private String script;

    @JSONField(ordinal = 21)
    private String scriptLanguage;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if(StringUtils.isEmpty(this.getEnvironmentId())){
            if(config.getConfig() != null){
                if(config.getProjectId() != null){
                    String evnId = config.getConfig().get(config.getProjectId()).getApiEnvironmentid();
                    this.setEnvironmentId(evnId);
                }else {
                    Collection<EnvironmentConfig> evnConfigList = config.getConfig().values();
                    if(evnConfigList!=null && !evnConfigList.isEmpty()){
                        for (EnvironmentConfig configItem : evnConfigList) {
                            String evnId = configItem.getApiEnvironmentid();
                            this.setEnvironmentId(evnId);
                            break;
                        }
                    }
                }
            }
        }
        //替换Metersphere环境变量
        script = StringUtils.replace(script,RunningParamKeys.API_ENVIRONMENT_ID,"\""+RunningParamKeys.RUNNING_PARAMS_PREFIX+this.getEnvironmentId()+".\"");

        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        JSR223PostProcessor processor = new JSR223PostProcessor();
        processor.setEnabled(this.isEnable());
        if (StringUtils.isNotEmpty(this.getName())) {
            processor.setName(this.getName());
        } else {
            processor.setName("JSR223PostProcessor");
        }
        processor.setProperty(TestElement.TEST_CLASS, JSR223PostProcessor.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        /*processor.setProperty("cacheKey", "true");*/
        processor.setProperty("scriptLanguage", this.getScriptLanguage());
        if (StringUtils.isNotEmpty(this.getScriptLanguage()) && this.getScriptLanguage().equals("nashornScript")) {
            processor.setProperty("scriptLanguage", "nashorn");
        }
        if (StringUtils.isNotEmpty(this.getScriptLanguage()) && this.getScriptLanguage().equals("rhinoScript")) {
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
