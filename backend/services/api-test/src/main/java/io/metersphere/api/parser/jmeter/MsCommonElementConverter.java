package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsCommonElementConverter extends AbstractJmeterElementConverter<MsCommonElement> {


    @Override
    public void toHashTree(HashTree tree, MsCommonElement element, ParameterConfig config) {
        // todo 解析前后置和断言
    }
}
