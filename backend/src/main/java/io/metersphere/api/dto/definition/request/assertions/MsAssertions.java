package io.metersphere.api.dto.definition.request.assertions;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.assertions.document.MsAssertionDocument;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.*;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "Assertions")
public class MsAssertions extends MsTestElement {
    private String clazzName = "io.metersphere.api.dto.definition.request.assertions.MsAssertions";

    private List<MsAssertionRegex> regex;
    private List<MsAssertionJsonPath> jsonPath;
    private List<MsAssertionJSR223> jsr223;
    private List<MsAssertionXPath2> xpath2;
    private MsAssertionDuration duration;
    private String type = "Assertions";
    private MsAssertionDocument document;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        addAssertions(tree);
    }

    private void addAssertions(HashTree hashTree) {
        // 增加JSON文档结构校验
        if (this.getDocument() != null && this.getDocument().getType().equals("JSON")) {
            if (StringUtils.isNotEmpty(this.getDocument().getData().getJsonFollowAPI()) && !this.getDocument().getData().getJsonFollowAPI().equals("false")) {
                ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
                this.getDocument().getData().setJson(apiDefinitionService.getDocument(this.getDocument().getData().getJsonFollowAPI(), "JSON"));
            }
            if (CollectionUtils.isNotEmpty(this.getDocument().getData().getJson())) {
                this.getDocument().getData().parseJson(hashTree, this.getName());
            }
        }
        // 增加XML文档结构校验
        if (this.getDocument() != null && this.getDocument().getType().equals("XML") && CollectionUtils.isNotEmpty(this.getDocument().getData().getXml())) {
            if (StringUtils.isNotEmpty(this.getDocument().getData().getXmlFollowAPI()) && !this.getDocument().getData().getXmlFollowAPI().equals("false")) {
                ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
                this.getDocument().getData().setXml(apiDefinitionService.getDocument(this.getDocument().getData().getXmlFollowAPI(), "XML"));
            }
            if (CollectionUtils.isNotEmpty(this.getDocument().getData().getXml())) {
                this.getDocument().getData().parseXml(hashTree, this.getName());
            }
        }

        if (CollectionUtils.isNotEmpty(this.getRegex())) {
            this.getRegex().stream().filter(MsAssertionRegex::isValid).forEach(assertion ->
                    hashTree.add(responseAssertion(assertion))
            );
        }

        if (CollectionUtils.isNotEmpty(this.getJsonPath())) {
            this.getJsonPath().stream().filter(MsAssertionJsonPath::isValid).forEach(assertion ->
                    hashTree.add(jsonPathAssertion(assertion))
            );
        }

        if (CollectionUtils.isNotEmpty(this.getXpath2())) {
            this.getXpath2().stream().filter(MsAssertionXPath2::isValid).forEach(assertion ->
                    hashTree.add(xPath2Assertion(assertion))
            );
        }

        if (CollectionUtils.isNotEmpty(this.getJsr223())) {
            this.getJsr223().stream().filter(MsAssertionJSR223::isValid).forEach(assertion ->
                    hashTree.add(jsr223Assertion(assertion))
            );
        }

        if (this.getDuration() != null && this.getDuration().isValid()) {
            hashTree.add(durationAssertion(this.getDuration()));
        }
    }

    private ResponseAssertion responseAssertion(MsAssertionRegex assertionRegex) {
        ResponseAssertion assertion = new ResponseAssertion();
        assertion.setEnabled(this.isEnable());
        if (StringUtils.isNotEmpty(assertionRegex.getDescription())) {
            assertion.setName(this.getName() + "==" + assertionRegex.getDescription());
        } else {
            assertion.setName(this.getName() + "==" + "AssertionRegex");
        }
        assertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("AssertionGui"));
        assertion.setAssumeSuccess(assertionRegex.isAssumeSuccess());
        assertion.addTestString(assertionRegex.getExpression());
        assertion.setToContainsType();
        switch (assertionRegex.getSubject()) {
            case "Response Code":
                assertion.setTestFieldResponseCode();
                break;
            case "Response Headers":
                assertion.setTestFieldResponseHeaders();
                break;
            case "Response Data":
                assertion.setTestFieldResponseData();
                break;
            default:
                break;
        }
        return assertion;
    }

    private JSONPathAssertion jsonPathAssertion(MsAssertionJsonPath assertionJsonPath) {
        JSONPathAssertion assertion = new JSONPathAssertion();
        assertion.setEnabled(this.isEnable());
        if (StringUtils.isNotEmpty(assertionJsonPath.getDescription())) {
            assertion.setName(this.getName() + "==" + assertionJsonPath.getDescription());
        } else {
            assertion.setName(this.getName() + "==" + "JSONPathAssertion");
        }
        assertion.setProperty(TestElement.TEST_CLASS, JSONPathAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("JSONPathAssertionGui"));
        assertion.setJsonPath(assertionJsonPath.getExpression());
        assertion.setExpectedValue(assertionJsonPath.getExpect());
        assertion.setJsonValidationBool(true);
        assertion.setExpectNull(false);
        assertion.setInvert(false);
        assertion.setProperty("ASS_OPTION", assertionJsonPath.getOption());
        assertion.setIsRegex(StringUtils.isEmpty(assertionJsonPath.getOption()) || "REGEX".equals(assertionJsonPath.getOption()));
        return assertion;
    }

    private XPath2Assertion xPath2Assertion(MsAssertionXPath2 assertionXPath2) {
        XPath2Assertion assertion = new XPath2Assertion();
        assertion.setEnabled(this.isEnable());
        if (StringUtils.isNotEmpty(assertionXPath2.getExpression())) {
            assertion.setName(this.getName() + "==" + assertionXPath2.getExpression());
        } else {
            assertion.setName(this.getName() + "==" + "XPath2Assertion");
        }
        assertion.setProperty(TestElement.TEST_CLASS, XPath2Assertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("XPath2AssertionGui"));
        assertion.setXPathString(assertionXPath2.getExpression());
        assertion.setNegated(false);
        return assertion;
    }

    private DurationAssertion durationAssertion(MsAssertionDuration assertionDuration) {
        DurationAssertion assertion = new DurationAssertion();
        assertion.setEnabled(this.isEnable());
        assertion.setName("" + "==" + "Response In Time: " + assertionDuration.getValue());
        if (StringUtils.isNotEmpty(this.getName())) {
            assertion.setName(this.getName() + "==" + "Response In Time: " + assertionDuration.getValue());
        }
        assertion.setProperty(TestElement.TEST_CLASS, DurationAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DurationAssertionGui"));
        assertion.setAllowedDuration(assertionDuration.getValue());
        return assertion;
    }

    private JSR223Assertion jsr223Assertion(MsAssertionJSR223 assertionJSR223) {
        JSR223Assertion assertion = new JSR223Assertion();
        assertion.setEnabled(this.isEnable());
        if (StringUtils.isNotEmpty(assertionJSR223.getDesc())) {
            assertion.setName("JSR223" + "==" + this.getName() + "==" + assertionJSR223.getDesc() + "&&" + assertionJSR223.getScript());
        } else {
            assertion.setName("JSR223" + "==" + this.getName() + "==" + "JSR223Assertion" + "&&" + assertionJSR223.getScript());
        }
        assertion.setProperty(TestElement.TEST_CLASS, JSR223Assertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        assertion.setProperty("cacheKey", "true");
        String scriptLanguage = assertionJSR223.getScriptLanguage();
        if (StringUtils.equals(scriptLanguage, "nashornScript")) {
            scriptLanguage = "nashorn";
        }
        if (StringUtils.equals(scriptLanguage, "rhinoScript")) {
            scriptLanguage = "rhino";
        }
        assertion.setProperty("scriptLanguage", scriptLanguage);
        assertion.setProperty("script", assertionJSR223.getScript());
        return assertion;
    }

}
