package io.metersphere.api.dto.definition.request.processors;

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
import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsJSR223Processor extends MsTestElement {
    private String type = ElementConstants.JSR223;
    private String clazzName = MsJSR223Processor.class.getCanonicalName();
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ScriptFilter.verify(this.getScriptLanguage(), this.getName(), script);
        ParameterConfig config = (ParameterConfig) msParameter;

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

        TestElement processor = new BeanShellSampler();
        if (jsrEnable == null || BooleanUtils.isTrue(jsrEnable)) {
            processor = new JSR223Sampler();
        }
        ScriptProcessorVO vo = new ScriptProcessorVO();
        BeanUtils.copyBean(vo, this);
        vo.setEnabled(this.isEnable());
        ElementUtil.initScript(processor, vo);

        String resourceId = StringUtils.isNotEmpty(this.getId()) ? this.getId() : this.getResourceId();
        ElementUtil.setBaseParams(processor, this.getParent(), config, resourceId, this.getIndex());

        // 失败重试
        HashTree jsr223PreTree;
        if (config.getRetryNum() > 0 && !ElementUtil.isLoop(this.getParent())) {
            final HashTree loopTree = ElementUtil.retryHashTree(this.getName(), config.getRetryNum(), tree);
            jsr223PreTree = loopTree.add(processor);
        } else {
            jsr223PreTree = tree.add(processor);
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(jsr223PreTree, el.getHashTree(), config);
            });
        }
    }
}
