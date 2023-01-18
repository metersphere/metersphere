package io.metersphere.api.dto.definition.request.processors.post;

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
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsJSR223PostProcessor extends MsTestElement {
    private String type = ElementConstants.JSR223_POST;
    private String clazzName = MsJSR223PostProcessor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        ScriptFilter.verify(this.getScriptLanguage(), this.getName(), script);
        if (config.isOperating()) {
            if (StringUtils.isNotEmpty(script) && script.startsWith(JMeterVars.class.getCanonicalName())) {
                return;
            }
        }
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        this.setEnvironmentId(ElementUtil.getScriptEnv(this.getEnvironmentId(), config, this.getProjectId()));

        TestElement processor = new BeanShellPostProcessor();
        if (jsrEnable == null || BooleanUtils.isTrue(jsrEnable)) {
            processor = new JSR223PostProcessor();
        }
        ScriptProcessorVO vo = new ScriptProcessorVO();
        BeanUtils.copyBean(vo, this);
        vo.setEnabled(this.isEnable());
        ElementUtil.initScript(processor, vo);

        final HashTree jsr223PostTree = tree.add(processor);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(jsr223PostTree, el.getHashTree(), config);
            });
        }
    }
}
