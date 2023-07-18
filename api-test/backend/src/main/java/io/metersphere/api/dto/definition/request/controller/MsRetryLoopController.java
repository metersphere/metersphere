package io.metersphere.api.dto.definition.request.controller;

import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.JSR223Listener;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsRetryLoopController extends MsTestElement {
    private String type = "RetryLoopController";
    private String clazzName = MsRetryLoopController.class.getCanonicalName();
    private final String template = """
            try {
                if (prev.isSuccess()) {
                    vars.put("%s", "STOPPED");
                }
                if (vars.get("%s") == null) {
                    vars.put("%s", "0");
                } else {
                    int retryNum = Integer.parseInt(vars.get("%s"));
                    log.info("重试：" + (retryNum + 1));
                    prev.setSampleLabel("MsRetry_" + (retryNum + 1) + "_" + prev.getSampleLabel());
                    retryNum++;
                    vars.put("%s", String.valueOf(retryNum));
                }
                if (vars.get("%s").equals("%s")) {
                    vars.put("%s", "STOPPED");
                }
            } catch (Exception e) {
                vars.put("%s", "STOPPED");
            }""";

    private long retryNum;
    private String ms_current_timer = UUID.randomUUID().toString();

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        final HashTree groupTree = controller(tree, this.getName());

        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.toHashTree(groupTree, el.getHashTree(), msParameter);
            });
        }
    }

    private WhileController initWhileController(String condition, String name) {
        if (StringUtils.isEmpty(condition)) {
            return null;
        }
        WhileController controller = new WhileController();
        controller.setEnabled(this.isEnable());
        controller.setName(StringUtils.join("RetryWhile_", name));
        controller.setProperty(TestElement.TEST_CLASS, WhileController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("WhileControllerGui"));
        controller.setCondition(condition);
        return controller;
    }

    private String script() {
        String stopStatus = ms_current_timer;
        String retryNum = StringUtils.join("VARS_", stopStatus);
        return String.format(template,
                stopStatus,
                retryNum,
                retryNum,
                retryNum,
                retryNum,
                retryNum,
                this.retryNum,
                stopStatus,
                stopStatus
        );
    }

    public HashTree controller(HashTree tree, String name) {
        String whileCondition = "${__jexl3(" + "\"${" + ms_current_timer + "}\" !=\"STOPPED\")}";
        HashTree hashTree = tree.add(initWhileController(whileCondition, name));
        // 添加超时处理，防止死循环
        JSR223Listener postProcessor = new JSR223Listener();
        postProcessor.setName("Retry-controller");
        postProcessor.setProperty(TestElement.TEST_CLASS, JSR223Listener.class.getName());
        postProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        postProcessor.setProperty("scriptLanguage", "groovy");
        postProcessor.setProperty("script", script());
        hashTree.add(postProcessor);
        return hashTree;
    }
}
