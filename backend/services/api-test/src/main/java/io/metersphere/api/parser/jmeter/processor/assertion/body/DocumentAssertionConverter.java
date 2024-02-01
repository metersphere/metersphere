package io.metersphere.api.parser.jmeter.processor.assertion.body;

import io.metersphere.project.api.assertion.body.MsDocumentAssertion;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-03  10:05
 */
public class DocumentAssertionConverter extends ResponseBodyTypeAssertionConverter<MsDocumentAssertion> {
    @Override
    public void parse(HashTree hashTree, MsDocumentAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus, boolean globalEnable) {
        if (msAssertion == null) {
            return;
        }
        // todo 定义好 jsonschema 再补充
//        String documentType = msAssertion.getDocumentType();
//        MsDocumentAssertionElement documentAssertionElement;
//        if (StringUtils.equals(documentType, MsDocumentAssertion.DocumentType.XML.name())) {
//            documentAssertionElement = msAssertion.getXmlAssertion();
//        } else {
//            documentAssertionElement = msAssertion.getJsonAssertion();
//        }
    }


}
