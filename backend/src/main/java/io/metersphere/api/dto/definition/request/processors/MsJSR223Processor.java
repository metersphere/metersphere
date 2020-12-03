package io.metersphere.api.dto.definition.request.processors;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "JSR223Processor")
public class MsJSR223Processor extends MsTestElement {
    private String type = "JSR223Processor";

    @JSONField(ordinal = 10)
    private String script;

    @JSONField(ordinal = 11)
    private String scriptLanguage;

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree) {
        JSR223Sampler processor = new JSR223Sampler();
        processor.setEnabled(true);
        processor.setName(this.getName() + "JSR223Processor");
        processor.setProperty(TestElement.TEST_CLASS, JSR223Sampler.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        processor.setProperty("cacheKey", "true");
        processor.setProperty("scriptLanguage", this.getScriptLanguage());
        processor.setProperty("script", this.getScript());

        final HashTree jsr223PreTree = tree.add(processor);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(jsr223PreTree, el.getHashTree());
            });
        }
    }
}
