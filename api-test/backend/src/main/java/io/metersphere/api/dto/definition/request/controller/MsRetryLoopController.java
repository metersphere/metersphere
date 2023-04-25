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
        String script = "import org.apache.commons.lang3.StringUtils;" +
                "\n// 失败重试控制" +
                "\n" + "try{" +
                "\n" + "\tint errorCount = prev.getErrorCount();" +
                "\n" + "\tif(errorCount == 0 && StringUtils.isBlank(prev.getFirstAssertionFailureMessage()) ){" +
                "\n" + "\t   vars.put(\"" + ms_current_timer + "\", \"stop\");\n" + "\t}" +
                "\n" + "\tif(vars.get(\"" + ms_current_timer + "_num\") == null){" +
                "\n" + "\t\tvars.put(\"" + ms_current_timer + "_num\", \"0\");" +
                "\n" + "\t}else{" +
                "\n" + "\t\tint retryNum= Integer.parseInt(vars.get(\"" + ms_current_timer + "_num\"));" +
                "\n" + "\t\tlog.info(\"重试：\"+ (retryNum + 1));" +
                "\n" + "        \tprev.setSampleLabel(\"MsRetry_\"+ (retryNum + 1) + \"_\" + prev.getSampleLabel());" +
                "\n" + "\t\tretryNum =retryNum +1;\n" + "\t\tvars.put(\"" + ms_current_timer + "_num\",retryNum + \"\");\n" + "\t}" +
                "\n" + "\tif(vars.get(\"" + ms_current_timer + "_num\").equals( \"" + retryNum + "\")){" +
                "\n" + "\t\tvars.put(\"" + ms_current_timer + "\", \"stop\");\n" + "\t}" +
                "\n" + "}catch (Exception e){" +
                "\n" + "\tvars.put(\"" + ms_current_timer + "\", \"stop\");\n" + "}" +
                "\n";
        return script;
    }

    public HashTree controller(HashTree tree, String name) {
        String whileCondition = "${__jexl3(" + "\"${" + ms_current_timer + "}\" !=\"stop\")}";
        HashTree hashTree = tree.add(initWhileController(whileCondition, name));
        // 添加超时处理，防止死循环
        JSR223Listener postProcessor = new JSR223Listener();
        postProcessor.setName("Retry-controller");
        postProcessor.setProperty(TestElement.TEST_CLASS, JSR223Listener.class.getName());
        postProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        postProcessor.setProperty("scriptLanguage", "beanshell");
        postProcessor.setProperty("script", script());
        hashTree.add(postProcessor);
        return hashTree;
    }
}
