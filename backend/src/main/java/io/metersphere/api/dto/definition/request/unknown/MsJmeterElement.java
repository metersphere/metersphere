package io.metersphere.api.dto.definition.request.unknown;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * 暂时存放所有未知的Jmeter Element对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "JmeterElement")
public class MsJmeterElement extends MsTestElement {
    private String type = "JmeterElement";
    private String elementType;
    private String jmeterElement;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        try {
            InputStream inputSource = getStrToStream(jmeterElement);
            if (inputSource != null) {
                Object scriptWrapper = SaveService.loadElement(inputSource);
                HashTree elementTree = tree;
                this.setElementType(scriptWrapper.getClass().getName());
                if (config.isOperating()) {
                    elementTree = tree.add(scriptWrapper);
                } else if (!(scriptWrapper instanceof TestPlan) && !(scriptWrapper instanceof ThreadGroup)) {
                    elementTree = tree.add(scriptWrapper);
                }
                if (scriptWrapper instanceof TestElement) {
                    ((TestElement) scriptWrapper).setName(this.getName());
                }
                if (CollectionUtils.isNotEmpty(hashTree)) {
                    for (MsTestElement el : hashTree) {
                        el.toHashTree(elementTree, el.getHashTree(), config);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static InputStream getStrToStream(String sInputString) {
        if (StringUtils.isNotEmpty(sInputString)) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
