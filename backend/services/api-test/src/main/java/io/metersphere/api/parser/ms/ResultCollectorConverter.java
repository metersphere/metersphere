package io.metersphere.api.parser.ms;


import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class ResultCollectorConverter extends AbstractMsElementConverter<ResultCollector> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, ResultCollector element, HashTree hashTree) {
        // resultController不做处理
    }
}
