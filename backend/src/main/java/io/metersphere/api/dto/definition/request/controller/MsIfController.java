package io.metersphere.api.dto.definition.request.controller;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "IfController")
public class MsIfController extends MsTestElement {
    private String type = "IfController";
    private String id;
    private String variable;
    private String operator;
    private String value;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        final HashTree groupTree = tree.add(ifController());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.toHashTree(groupTree, el.getHashTree(), config);
            });
        }
    }

    private IfController ifController() {
        IfController ifController = new IfController();
        ifController.setEnabled(this.isEnable());
        if (StringUtils.isEmpty(this.getName())) {
            this.setName(getLabelName());
        }
        ifController.setName(this.getName());
        ifController.setProperty(TestElement.TEST_CLASS, IfController.class.getName());
        ifController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("IfControllerPanel"));
        ifController.setCondition(this.getCondition());
        ifController.setEvaluateAll(false);
        ifController.setUseExpression(true);
        return ifController;
    }

    public boolean isValid() {
        if (StringUtils.contains(operator, "empty")) {
            return StringUtils.isNotBlank(variable);
        }
        return StringUtils.isNotBlank(variable) && StringUtils.isNotBlank(operator) && StringUtils.isNotBlank(value);
    }

    public String getLabelName() {
        if (isValid()) {
            String label = "条件控制器：" + variable + " " + operator;
            if (StringUtils.isNotBlank(value)) {
                label += " " + this.value;
            }
            return label;
        }
        return "IfController";
    }

    public String getCondition() {
        String variable = "\"" + this.variable + "\"";
        String operator = this.operator;
        String value;
        if (StringUtils.equals(operator, "<") || StringUtils.equals(operator, ">")) {
            value = this.value;
        } else {
            value = "\"" + this.value + "\"";
        }
        if (StringUtils.contains(operator, "~")) {
            value = "\".*" + this.value + ".*\"";
        }

        if (StringUtils.equals(operator, "is empty")) {
            variable = variable + "==" + "\"\\" + this.variable + "\"" + "|| empty(" + variable + ")";
            operator = "";
            value = "";
        }

        if (StringUtils.equals(operator, "is not empty")) {
            variable = variable + "!=" + "\"\\" + this.variable + "\"" + "&& !empty(" + variable + ")";
            operator = "";
            value = "";
        }

        return "${__jexl3(" + variable + operator + value + ")}";
    }

}
