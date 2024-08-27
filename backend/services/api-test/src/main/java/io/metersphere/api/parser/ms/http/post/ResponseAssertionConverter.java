package io.metersphere.api.parser.ms.http.post;

import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jorphan.collections.HashTree;

public class ResponseAssertionConverter extends AbstractMsElementConverter<ResponseAssertion> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, ResponseAssertion element, HashTree hashTree) {

    }
}
