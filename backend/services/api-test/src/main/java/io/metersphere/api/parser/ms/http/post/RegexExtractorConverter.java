package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.extract.RegexExtract;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jorphan.collections.HashTree;

public class RegexExtractorConverter extends AbstractMsElementConverter<RegexExtractor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, RegexExtractor element, HashTree hashTree) {
        RegexExtract regexExtract = new RegexExtract();
        regexExtract.setVariableName(element.getRefName());
        regexExtract.setExpression(element.getRegex());
        regexExtract.setEnable(element.isEnabled());
        regexExtract.setExtractScope(this.getUseField(element));
        regexExtract.setResultMatchingRuleNum(element.getMatchNumber());
        regexExtract.setVariableType("TEMPORARY");
        ConverterUtils.addPostExtract(parent, regexExtract);
    }

    private String getUseField(RegexExtractor element) {
        String useHeaders = element.getPropertyAsString("RegexExtractor.useHeaders");
        if (StringUtils.equalsIgnoreCase(useHeaders, "false")) {
            return RegexExtract.ExtractScope.BODY.name();
        } else if (StringUtils.equalsIgnoreCase(useHeaders, "unescaped")) {
            return RegexExtract.ExtractScope.UNESCAPED_BODY.name();
        } else if (StringUtils.equalsIgnoreCase(useHeaders, "as_document")) {
            return RegexExtract.ExtractScope.BODY_AS_DOCUMENT.name();
        } else if (StringUtils.equalsIgnoreCase(useHeaders, "true")) {
            return RegexExtract.ExtractScope.RESPONSE_HEADERS.name();
        } else if (StringUtils.equalsIgnoreCase(useHeaders, "request_headers")) {
            return RegexExtract.ExtractScope.REQUEST_HEADERS.name();
        } else if (StringUtils.equalsIgnoreCase(useHeaders, "URL")) {
            return RegexExtract.ExtractScope.URL.name();
        } else if (StringUtils.equalsIgnoreCase(useHeaders, "code")) {
            return RegexExtract.ExtractScope.RESPONSE_CODE.name();
        } else if (StringUtils.equalsIgnoreCase(useHeaders, "message")) {
            return RegexExtract.ExtractScope.RESPONSE_MESSAGE.name();
        }
        return useHeaders;
    }
}
