package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.request.MsJMeterComponent;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsJMeterComponentConverter extends AbstractJmeterElementConverter<MsJMeterComponent> {

    @Override
    public void toHashTree(HashTree tree, MsJMeterComponent jMeterComponent, ParameterConfig msParameter) {
        HashTree elementTree = null;
        try (InputStream inputSource = getStrToStream(jMeterComponent.getTestElementContent())) {
            if (inputSource != null) {
                Object scriptWrapper = SaveService.loadElement(inputSource);
                if (scriptWrapper instanceof TestElement) {
                    ((TestElement) scriptWrapper).setName(jMeterComponent.getName());
                    ((TestElement) scriptWrapper).setEnabled(jMeterComponent.getEnable());
                }
                elementTree = tree.add(scriptWrapper);
            }
        } catch (Exception ignore) {
        }
        if (elementTree != null) {
            parseChild(elementTree, jMeterComponent, msParameter);
        } else {
            parseChild(tree, jMeterComponent, msParameter);
        }

    }

    public static InputStream getStrToStream(String sInputString) {
        if (StringUtils.isNotEmpty(sInputString)) {
            return new ByteArrayInputStream(sInputString.getBytes());
        }
        return null;
    }

}
