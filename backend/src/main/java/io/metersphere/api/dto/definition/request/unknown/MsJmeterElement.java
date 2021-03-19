package io.metersphere.api.dto.definition.request.unknown;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.commons.utils.LogUtil;
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
            // 非导出操作，且不是启用状态则跳过执行
            if (!config.isOperating() && !this.isEnable()) {
                return;
            }
            InputStream inputSource = getStrToStream(jmeterElement);
            if (inputSource != null) {
                Object scriptWrapper = SaveService.loadElement(inputSource);
                HashTree elementTree = tree;
                this.setElementType(scriptWrapper.getClass().getName());
                if (scriptWrapper instanceof TestElement) {
                    ((TestElement) scriptWrapper).setName(this.getName());
                    ((TestElement) scriptWrapper).setEnabled(this.isEnable());
                }
                if (config.isOperating()) {
                    elementTree = tree.add(scriptWrapper);
                } else if (!(scriptWrapper instanceof TestPlan) && !(scriptWrapper instanceof ThreadGroup)) {
                    elementTree = tree.add(scriptWrapper);
                }
                if (!config.isOperating() && scriptWrapper instanceof ThreadGroup && !((ThreadGroup) scriptWrapper).isEnabled()) {
                    LogUtil.info(((ThreadGroup) scriptWrapper).getName() + "是被禁用线程组不加入执行");
                } else {
                    if (CollectionUtils.isNotEmpty(hashTree)) {
                        for (MsTestElement el : hashTree) {
                            el.toHashTree(elementTree, el.getHashTree(), config);
                        }
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
