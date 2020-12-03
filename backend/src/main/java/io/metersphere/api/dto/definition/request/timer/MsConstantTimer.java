package io.metersphere.api.dto.definition.request.timer;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "ConstantTimer")
public class MsConstantTimer extends MsTestElement {
    private String type = "ConstantTimer";
    private String id;
    private boolean enable = true;
    private String delay;

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree) {
        final HashTree groupTree = tree.add(constantTimer());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(groupTree, el.getHashTree());
            });
        }
    }

    private ConstantTimer constantTimer() {
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(true);
        constantTimer.setName(this.getDelay() + " ms");
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ConstantTimerGui"));
        constantTimer.setDelay(this.getDelay());
        return constantTimer;
    }

}
