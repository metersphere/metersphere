package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 *
 * 脚本解析器
 */
public class MsHTTPElementConverter extends AbstractJmeterElementConverter<MsHTTPElement> {

    @Override
    public void toHashTree(HashTree tree, MsHTTPElement msHTTPElement, ParameterConfig msParameter) {
        ParameterConfig config = msParameter;

        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setName(msHTTPElement.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));

        HashTree httpTree = tree.add(sampler);
        parseChild(httpTree, msHTTPElement, config);
    }
}
