package io.metersphere.api.parser.ms.http;

import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.collections.HashTree;

import java.util.LinkedList;
import java.util.List;

public class HeaderManagerConverter extends AbstractMsElementConverter<HeaderManager> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, HeaderManager element, HashTree hashTree) {
        if (parent instanceof MsHTTPElement msHTTPElement) {
            // 处理HTTP协议的请求头
            List<MsHeader> headerKvList = msHTTPElement.getHeaders() == null ? new LinkedList<>() : msHTTPElement.getHeaders();
            CollectionProperty collectionProperty = element.getHeaders();
            List<String> extendsHeaderKey = headerKvList.stream().map(MsHeader::getKey).toList();
            for (int i = 0; i < collectionProperty.size(); i++) {
                JMeterProperty jMeterProperty = collectionProperty.get(i);
                String key = jMeterProperty.getName();
                if (!extendsHeaderKey.contains(key)) {
                    headerKvList.add(new MsHeader() {{
                        this.setKey(key);
                        this.setValue(jMeterProperty.getStringValue());
                    }});
                }
            }
            msHTTPElement.setHeaders(headerKvList);
        }
    }
}
