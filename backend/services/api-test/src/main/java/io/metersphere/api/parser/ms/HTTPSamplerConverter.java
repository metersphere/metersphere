package io.metersphere.api.parser.ms;

import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  11:36
 */
public class HTTPSamplerConverter extends AbstractMsElementConverter<HTTPSamplerProxy> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, HTTPSamplerProxy httpSampler, HashTree hashTree) {
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        // todo 解析HTTP请求
        parent.getChildren().add(msHTTPElement);
        parseChild(msHTTPElement, httpSampler, hashTree);
    }
}
