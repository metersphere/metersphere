package io.metersphere.api.dto.definition.request.timer;

import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsConstantTimer extends MsTestElement {
    private String type = ElementConstants.CONSTANT_TIMER;
    private String clazzName = MsConstantTimer.class.getCanonicalName();
    private String id;
    private String delay;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable() && MapUtils.isEmpty(config.getKeyMap())) {
            return;
        }
        if (!ElementUtil.isEnable(this, config)) {
            return;
        }
        final HashTree groupTree = tree.add(constantTimer());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(groupTree, el.getHashTree(), config);
            });
        }
    }

    private ConstantTimer constantTimer() {
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(this.isEnable());
        constantTimer.setName(this.getDelay() + " ms");
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ConstantTimerGui"));
        constantTimer.setDelay(this.getDelay());
        return constantTimer;
    }
}
