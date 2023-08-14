package io.metersphere.api.dto.definition.request.assertions.document;

import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.utils.JSON;
import io.metersphere.vo.Condition;
import io.metersphere.vo.ElementCondition;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.assertions.XMLAssertion;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class Document {
    private String jsonFollowAPI;
    private String xmlFollowAPI;
    private List<DocumentElement> json;
    private List<DocumentElement> xml;
    private String assertionName;
    private boolean include = false;
    private boolean typeVerification = false;

    private static final String delimiter = "split==";

    public void parseJson(HashTree hashTree, String name) {
        this.assertionName = name;
        // 提取出合并的权限
        Map<String, ElementCondition> conditionMap = new HashMap<>();
        conditions(this.getJson(), conditionMap);
        // 数据处理生成断言条件
        List<JSONPathAssertion> list = new LinkedList<>();
        formatting(this.getJson(), list, null, conditionMap);

        if (CollectionUtils.isNotEmpty(list)) {
            hashTree.add(list);
        }
    }

    public void parseXml(HashTree hashTree, String name) {
        this.assertionName = name;
        // 提取出合并的权限
        Map<String, ElementCondition> conditionMap = new HashMap<>();
        conditions(this.getXml(), conditionMap);
        // 数据处理生成断言条件
        List<XMLAssertion> list = new LinkedList<>();
        xmlFormatting(this.getXml(), list, null, conditionMap);

        if (CollectionUtils.isNotEmpty(list)) {
            hashTree.add(list);
        }
    }

    private void conditions(List<DocumentElement> dataList, Map<String, ElementCondition> conditionMap) {
        dataList.forEach(item -> {
            if (StringUtils.isEmpty(item.getGroupId())) {
                ElementCondition elementCondition = new ElementCondition(item.isInclude(), item.isTypeVerification(), item.isArrayVerification(), new LinkedList<Condition>() {{
                    this.add(new Condition(item.getContentVerification(), item.getExpectedOutcome()));
                }});
                elementCondition.setType(item.getType());
                conditionMap.put(item.getId(), elementCondition);
            } else {
                if (conditionMap.containsKey(item.getGroupId())) {
                    conditionMap.get(item.getGroupId()).getConditions().add(new Condition(item.getContentVerification(), item.getExpectedOutcome()));
                } else {
                    ElementCondition elementCondition = new ElementCondition(item.isInclude(), item.isTypeVerification(), item.isArrayVerification(), new LinkedList<Condition>() {{
                        this.add(new Condition(item.getContentVerification(), item.getExpectedOutcome()));
                    }});
                    elementCondition.setType(item.getType());
                    conditionMap.put(item.getGroupId(), elementCondition);
                }
            }
            if (CollectionUtils.isNotEmpty(item.getChildren())) {
                conditions(item.getChildren(), conditionMap);
            }
        });
    }

    public void formatting(List<DocumentElement> dataList, List<JSONPathAssertion> list, DocumentElement parentNode, Map<String, ElementCondition> conditionMap) {
        for (int i = 0; i < dataList.size(); i++) {
            DocumentElement item = dataList.get(i);
            if (StringUtils.isEmpty(item.getGroupId())) {
                if (!item.getId().equals(PropertyConstant.ROOT)) {
                    if (parentNode != null) {
                        if (parentNode.getType().equals(PropertyConstant.ARRAY)) {
                            try {
                                int index = StringUtils.isNotEmpty(item.getName()) ? Integer.parseInt(item.getName()) : i;
                                item.setJsonPath(parentNode.getJsonPath() + "[" + index + "]");
                            } catch (Exception e) {
                                item.setJsonPath(parentNode.getJsonPath() + "." + item.getName());
                            }
                        } else {
                            item.setJsonPath(parentNode.getJsonPath() + "." + item.getName());
                        }
                    } else {
                        item.setJsonPath("$." + item.getName());
                    }
                    if (!StringUtils.equalsAny(item.getContentVerification(), PropertyConstant.NONE, null) || item.isInclude() || item.isArrayVerification() || item.isTypeVerification()) {
                        list.add(newJSONPathAssertion(item, conditionMap.get(item.getId())));
                    }
                    if (CollectionUtils.isNotEmpty(item.getChildren())) {
                        if (item.getType().equals(PropertyConstant.ARRAY) && !item.isArrayVerification()) {
                            continue;
                        }
                        formatting(item.getChildren(), list, item, conditionMap);
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(item.getChildren())) {
                        formatting(item.getChildren(), list, null, conditionMap);
                    }
                }
            }
        }
    }

    public void xmlFormatting(List<DocumentElement> dataList, List<XMLAssertion> list, DocumentElement parentNode, Map<String, ElementCondition> conditionMap) {
        for (DocumentElement item : dataList) {
            if (StringUtils.isEmpty(item.getGroupId())) {
                if (parentNode != null) {
                    if (StringUtils.equals(parentNode.getType(), PropertyConstant.ARRAY) && item.getType().equals(PropertyConstant.STRING)) {
                        try {
                            int index = StringUtils.isNotEmpty(item.getName()) ? Integer.parseInt(item.getName()) : 0;
                            item.setJsonPath(parentNode.getJsonPath() + "[" + index + "]");
                        } catch (Exception e) {
                            item.setJsonPath(parentNode.getJsonPath() + "." + item.getName());
                        }
                    } else {
                        item.setJsonPath(parentNode.getJsonPath() + "." + item.getName());
                    }
                } else {
                    item.setJsonPath("$." + item.getName());
                }
                if (!StringUtils.equalsAny(item.getContentVerification(), PropertyConstant.NONE, null) || item.isInclude() || item.isArrayVerification() || item.isTypeVerification()) {
                    list.add(newXMLAssertion(item, conditionMap.get(item.getId())));
                }
                if (CollectionUtils.isNotEmpty(item.getChildren())) {
                    if (item.getType().equals(PropertyConstant.ARRAY) && !item.isArrayVerification()) {
                        continue;
                    }
                    xmlFormatting(item.getChildren(), list, item, conditionMap);
                }
            }
        }
    }

    private String getConditionStr(DocumentElement item, ElementCondition elementCondition) {
        StringBuilder conditionStr = new StringBuilder();
        if (elementCondition != null && CollectionUtils.isNotEmpty(elementCondition.getConditions())) {
            if (elementCondition.isTypeVerification()) {
                conditionStr.append("类型：[ " + item.getType() + " ]");
                conditionStr.append(" and ");
            }
            elementCondition.getConditions().forEach(condition -> {
                if (!StringUtils.equals(PropertyConstant.NONE, condition.getKey())) {
                    conditionStr.append(item.getLabel(condition.getKey()).replace("'%'", (condition.getValue() != null ? condition.getValue().toString() : "")));
                    conditionStr.append(" and ");
                }
            });
        }
        String label = "";
        if (StringUtils.isNotEmpty(conditionStr.toString())) {
            label = conditionStr.toString().substring(0, conditionStr.toString().length() - 4);
        }
        return label;
    }

    private JSONPathAssertion newJSONPathAssertion(DocumentElement item, ElementCondition elementCondition) {
        JSONPathAssertion assertion = new JSONPathAssertion();
        assertion.setEnabled(true);
        assertion.setJsonValidationBool(true);
        assertion.setExpectNull(false);
        assertion.setInvert(false);

        assertion.setName((StringUtils.isNotEmpty(assertionName) ? assertionName : "DocumentAssertion") + (delimiter + item.getJsonPath() + StringUtils.SPACE + getConditionStr(item, elementCondition)));
        assertion.setProperty(TestElement.TEST_CLASS, JSONPathAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("JSONPathAssertionGui"));
        assertion.setJsonPath(item.getJsonPath());
        assertion.setExpectedValue(item.getExpectedOutcome() != null ? item.getExpectedOutcome().toString() : "");
        assertion.setProperty(PropertyConstant.ASS_OPTION, "DOCUMENT");
        assertion.setProperty(PropertyConstant.ELEMENT_CONDITION, JSON.toJSONString(elementCondition));

        if (StringUtils.isEmpty(item.getContentVerification()) || "regular".equals(item.getContentVerification())) {
            assertion.setIsRegex(true);
        } else {
            assertion.setIsRegex(false);
        }
        return assertion;
    }

    private XMLAssertion newXMLAssertion(DocumentElement item, ElementCondition elementCondition) {
        XMLAssertion assertion = new XMLAssertion();
        assertion.setEnabled(true);
        assertion.setName((StringUtils.isNotEmpty(assertionName) ? assertionName : "XMLAssertion") + delimiter + (item.getJsonPath() + StringUtils.SPACE + getConditionStr(item, elementCondition)));
        assertion.setProperty(TestElement.TEST_CLASS, XMLAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("XMLAssertionGui"));
        assertion.setProperty(PropertyConstant.XML_PATH, item.getJsonPath());
        assertion.setProperty(PropertyConstant.EXPECTED_VALUE, item.getExpectedOutcome() != null ? item.getExpectedOutcome().toString() : "");
        assertion.setProperty(PropertyConstant.ELEMENT_CONDITION, JSON.toJSONString(elementCondition));
        return assertion;
    }
}
