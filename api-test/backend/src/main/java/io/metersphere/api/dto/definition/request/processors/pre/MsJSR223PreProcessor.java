package io.metersphere.api.dto.definition.request.processors.pre;

import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.shell.filter.ScriptFilter;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.vo.ScriptProcessorVO;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.JMeterVars;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsJSR223PreProcessor extends MsTestElement {
    private String type = ElementConstants.JSR223_PRE;
    private String clazzName = MsJSR223PreProcessor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        if (config.isOperating()) {
            if (StringUtils.isNotEmpty(script) && script.startsWith(JMeterVars.class.getCanonicalName())) {
                return;
            }
        }
        ScriptFilter.verify(this.getScriptLanguage(), this.getName(), script);
        this.setEnvironmentId(ElementUtil.getScriptEnv(this.getEnvironmentId(), config, this.getProjectId()));

        final HashTree jsr223PreTree = tree.add(getShellProcessor());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(jsr223PreTree, el.getHashTree(), config);
            });
        }
    }

    public TestElement getShellProcessor() {
        TestElement processor = new BeanShellPreProcessor();
        if (jsrEnable == null || BooleanUtils.isTrue(jsrEnable)) {
            processor = new JSR223PreProcessor();
        }
        ScriptProcessorVO vo = new ScriptProcessorVO();
        BeanUtils.copyBean(vo, this);
        vo.setEnabled(this.isEnable());
        ElementUtil.initScript(processor, vo);
        return processor;
    }
}
