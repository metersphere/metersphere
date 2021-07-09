package io.metersphere.api.dto.definition.request.controller;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "TransactionController")
public class MsTransactionController extends MsTestElement {
    private String type = "TransactionController";
    private String name;
    private boolean generateParentSample;
    private boolean includeTimers;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        final HashTree groupTree = tree.add(transactionController());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.toHashTree(groupTree, el.getHashTree(), config);
            });
        }
    }

    private TransactionController transactionController() {
        TransactionController transactionController = new TransactionController();
        transactionController.setEnabled(this.isEnable());
        if (StringUtils.isEmpty(this.getName())) {
            this.setName(getLabelName());
        }
        transactionController.setName(this.getName());
        transactionController.setProperty(TestElement.TEST_CLASS, TransactionController.class.getName());
        transactionController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TransactionControllerGui"));
        transactionController.setGenerateParentSample(generateParentSample);
        transactionController.setIncludeTimers(includeTimers);
        return transactionController;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(name);
    }

    public String getLabelName() {
        if (isValid()) {
            String label = "事务控制器：";
            if (StringUtils.isNotBlank(name)) {
                label += " " + this.name;
            }
            return label;
        }
        return "TransactionController";
    }
}
