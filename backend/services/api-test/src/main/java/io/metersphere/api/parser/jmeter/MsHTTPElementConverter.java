package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.parser.jmeter.body.MsBodyConverter;
import io.metersphere.api.parser.jmeter.body.MsBodyConverterFactory;
import io.metersphere.api.parser.jmeter.body.MsFormDataBodyConverter;
import io.metersphere.api.parser.jmeter.body.MsWWWFormBodyConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.springframework.http.HttpMethod;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsHTTPElementConverter extends AbstractJmeterElementConverter<MsHTTPElement> {

    private ParameterConfig config;

    @Override
    public void toHashTree(HashTree tree, MsHTTPElement msHTTPElement, ParameterConfig config) {
        if (BooleanUtils.isFalse(msHTTPElement.getEnable())) {
            LogUtils.info("MsHTTPElement is disabled");
            return;
        }
        this.config = config;
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setName(msHTTPElement.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));

        sampler.setMethod(msHTTPElement.getMethod());
        // todo 根据环境设置
        sampler.setDomain(msHTTPElement.getUrl());
        sampler.setPath(msHTTPElement.getPath());

        handleBody(sampler, msHTTPElement);
        HashTree httpTree = tree.add(sampler);
        parseChild(httpTree, msHTTPElement, config);
    }

    /**
     * 解析body参数
     *
     * @param sampler
     * @param msHTTPElement
     */
    private void handleBody(HTTPSamplerProxy sampler, MsHTTPElement msHTTPElement) {
        Body body = msHTTPElement.getBody();
        // 请求体处理
        if (body != null) {
            Class bodyClass = body.getBodyClassByType();
            MsBodyConverter converter = MsBodyConverterFactory.getConverter(bodyClass);

            // 这里get请求，不处理 form-date 和 www-form-urlencoded 类型的参数
            // 否则会被 jmeter 作为 query 参数
            if (StringUtils.equalsIgnoreCase(msHTTPElement.getMethod(), HttpMethod.GET.name())
                    && (converter instanceof MsWWWFormBodyConverter || converter instanceof MsFormDataBodyConverter)) {
                return;
            }
            converter.parse(sampler, body.getBodyDataByType(), config);
        }
    }
}
