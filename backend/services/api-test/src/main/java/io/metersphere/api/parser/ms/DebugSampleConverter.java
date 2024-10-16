package io.metersphere.api.parser.ms;


import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class DebugSampleConverter extends AbstractMsElementConverter<DebugSampler> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, DebugSampler element, HashTree hashTree) {
        // debug不做处理
    }
}
