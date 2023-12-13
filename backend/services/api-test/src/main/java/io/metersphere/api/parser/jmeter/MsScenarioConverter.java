package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 *
 * 脚本解析器
 */
public class MsScenarioConverter extends AbstractJmeterElementConverter<MsScenario> {

    @Override
    public void toHashTree(HashTree tree, MsScenario msScenario, ParameterConfig msParameter) {
        ParameterConfig config = msParameter;

        parseChild(tree, msScenario, config);
    }
}
