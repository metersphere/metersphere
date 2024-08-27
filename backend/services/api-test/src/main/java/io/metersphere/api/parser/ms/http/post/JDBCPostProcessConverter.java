package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPostProcessor;
import org.apache.jorphan.collections.HashTree;

public class JDBCPostProcessConverter extends AbstractMsElementConverter<JDBCPostProcessor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, JDBCPostProcessor element, HashTree hashTree) {
        ConverterUtils.addPostProcess(parent, ConverterUtils.genJDBCProcessor(element));
    }
}
