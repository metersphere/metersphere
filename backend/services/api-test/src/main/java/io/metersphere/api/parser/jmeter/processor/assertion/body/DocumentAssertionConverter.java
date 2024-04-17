package io.metersphere.api.parser.jmeter.processor.assertion.body;

import io.metersphere.assertions.JSONPathAssertion;
import io.metersphere.assertions.XMLAssertion;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.body.Condition;
import io.metersphere.project.api.assertion.body.ElementCondition;
import io.metersphere.project.api.assertion.body.MsDocumentAssertion;
import io.metersphere.project.api.assertion.body.MsDocumentAssertionElement;
import io.metersphere.project.constants.PropertyConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        if (needParse(msAssertion, config)) {
            String documentType = msAssertion.getDocumentType();
            if (StringUtils.equals(documentType, MsDocumentAssertion.DocumentType.XML.name())) {
                hashTree.add(parseAssertion(msAssertion.getXmlAssertion(), globalEnable, new XMLAssertion()));
            } else {
                hashTree.add(parseAssertion(msAssertion.getJsonAssertion(), globalEnable, new JSONPathAssertion()));
            }
        }

    }

    private <T extends TestElement> List<T> parseAssertion(MsDocumentAssertionElement assertion, boolean globalEnable, T t) {
        if (assertion == null) {
            return List.of();
        }
        List<T> list = new LinkedList<>();
        Map<String, ElementCondition> conditionMap = new java.util.HashMap<>();
        List<MsDocumentAssertionElement> dataList = new ArrayList<>();
        dataList.add(assertion);
        conditions(dataList, conditionMap);
        formatting(dataList, list, null, conditionMap, globalEnable, t);
        return list;
    }

    private void conditions(List<MsDocumentAssertionElement> dataList, Map<String, ElementCondition> conditionMap) {
        dataList.forEach(item -> {
            ElementCondition elementCondition =
                    ElementCondition.builder()
                            .include(item.getInclude())
                            .typeVerification(item.getTypeVerification())
                            .arrayVerification(item.getArrayVerification())
                            .type(item.getType())
                            .conditions(new LinkedList<Condition>() {{
                                this.add(new Condition(item.getCondition(), item.getExpectedResult()));
                            }}).build();
            conditionMap.put(item.getId(), elementCondition);

            if (CollectionUtils.isNotEmpty(item.getChildren())) {
                conditions(item.getChildren(), conditionMap);
            }
        });
    }

    public <T extends TestElement> void formatting(List<MsDocumentAssertionElement> dataList,
                                                   List<T> list,
                                                   MsDocumentAssertionElement parentNode,
                                                   Map<String, ElementCondition> conditionMap,
                                                   boolean globalEnable,
                                                   T t) {
        for (int i = 0; i < dataList.size(); i++) {
            MsDocumentAssertionElement item = dataList.get(i);
            if (!item.getId().equals(PropertyConstant.ROOT)) {
                if (parentNode != null) {
                    if (parentNode.getType().equals(PropertyConstant.ARRAY)) {
                        try {
                            int index = StringUtils.isNotEmpty(item.getParamName()) ? Integer.parseInt(item.getParamName()) : i;
                            item.setJsonPath(parentNode.getJsonPath() + "[" + index + "]");
                        } catch (Exception e) {
                            item.setJsonPath(parentNode.getJsonPath() + "." + item.getParamName());
                        }
                    } else {
                        item.setJsonPath(parentNode.getJsonPath() + "." + item.getParamName());
                    }
                } else {
                    item.setJsonPath("$." + item.getParamName());
                }
                if (!StringUtils.equalsAny(item.getCondition(), PropertyConstant.NONE, null) || item.getInclude() || item.getArrayVerification() || item.getTypeVerification()) {
                    list.add(newAssertion(item, conditionMap.get(item.getId()), globalEnable, t));
                }
                if (CollectionUtils.isNotEmpty(item.getChildren())) {
                    if (item.getType().equals(PropertyConstant.ARRAY) && !item.getArrayVerification()) {
                        continue;
                    }
                    formatting(item.getChildren(), list, item, conditionMap, globalEnable, t);
                }
            } else {
                if (CollectionUtils.isNotEmpty(item.getChildren())) {
                    formatting(item.getChildren(), list, null, conditionMap, globalEnable, t);
                }
            }
        }
    }

    private String getConditionStr(MsDocumentAssertionElement item, ElementCondition elementCondition) {
        StringBuilder conditionStr = new StringBuilder();
        if (elementCondition != null && CollectionUtils.isNotEmpty(elementCondition.getConditions())) {
            if (elementCondition.isTypeVerification()) {
                conditionStr.append("类型：[ ").append(item.getType()).append(" ]");
                conditionStr.append(" and ");
            }
            elementCondition.getConditions().forEach(condition -> {
                if (!StringUtils.equals(PropertyConstant.NONE, condition.getKey())) {
                    conditionStr.append(getLabel(condition.getKey()).replace("'%'", (condition.getValue() != null ? condition.getValue().toString() : "")));
                    conditionStr.append(" and ");
                }
            });
        }
        String label = "";
        if (StringUtils.isNotEmpty(conditionStr.toString())) {
            label = conditionStr.substring(0, conditionStr.toString().length() - 4);
        }
        return label;
    }

    private <T extends TestElement> T newAssertion(MsDocumentAssertionElement item, ElementCondition elementCondition, boolean globalEnable, T assertion) {
        assertion.setEnabled(true);
        if (BooleanUtils.isFalse(globalEnable)) {
            // 如果整体禁用，则禁用
            assertion.setEnabled(false);
        }
        if (assertion instanceof XMLAssertion xmlAssertion) {
            xmlAssertion.setJsonValidationBool(true);
        }

        assertion.setName(String.format("Response date expect %s %s %s", item.getJsonPath(), getConditionStr(item, elementCondition).toLowerCase().replace("_", ""), item.getExpectedResult()));
        assertion.setProperty(TestElement.TEST_CLASS, assertion.getClass().getName());
        assertion.setProperty(TestElement.GUI_CLASS, "io.metersphere.assertions.gui." + assertion.getClass().getSimpleName() + "Gui");
        assertion.setProperty("JSON_PATH", item.getJsonPath());
        assertion.setProperty(PropertyConstant.EXPECTED_VALUE, item.getExpectedResult() != null ? item.getExpectedResult().toString() : "");
        assertion.setProperty("CONDITION", item.getCondition());
        assertion.setProperty("JSONVALIDATION", true);
        return assertion;
    }

    public String getLabel(String value) {
        return switch (value) {
            case "EQUALS" -> "值-等于[value='%']";
            case "NOT_EQUALS" -> "值-不等于[value!='%']";
            case "CONTAINS" -> "值-包含[include='%']";
            case "LENGTH_EQUALS" -> "长度-等于[length='%']";
            case "LENGTH_NOT_EQUALS" -> "长度-不等于[length!='%']";
            case "LENGTH_GT" -> "长度-大于[length>'%']";
            case "LENGTH_LT" -> "长度-小于[length<'%']";
            case "REGEX" -> "正则匹配";
            default -> "不校验[]";
        };
    }


}
