package io.metersphere.api.dto.definition.request.controller;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.controller.loop.CountController;
import io.metersphere.api.dto.definition.request.controller.loop.MsForEachController;
import io.metersphere.api.dto.definition.request.controller.loop.MsWhileController;
import io.metersphere.api.dto.shell.filter.ScriptFilter;
import io.metersphere.commons.constants.LoopConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.*;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.reporters.ResultAction;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "LoopController")
public class MsLoopController extends MsTestElement {
    private String type = "LoopController";
    private String clazzName = MsLoopController.class.getCanonicalName();

    @JSONField(ordinal = 20)
    private String loopType;

    @JSONField(ordinal = 21)
    private CountController countController;

    @JSONField(ordinal = 22)
    private MsForEachController forEachController;

    @JSONField(ordinal = 23)
    private MsWhileController whileController;


    private String ms_current_timer = UUID.randomUUID().toString();

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;

        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        final HashTree groupTree = controller(tree);
        if (CollectionUtils.isNotEmpty(config.getVariables())) {
            ElementUtil.addCsvDataSet(groupTree, config.getVariables(), config, "shareMode.thread");
            ElementUtil.addCounter(groupTree, config.getVariables(), true);
            ElementUtil.addRandom(groupTree, config.getVariables());
        }

        // 循环下都增加一个计数器，用于结果统计
        groupTree.add(addCounterConfig());
        // 不打开执行成功后轮询功能，则成功后就停止循环
        if (StringUtils.equals(this.loopType, LoopConstants.LOOP_COUNT.name()) && this.countController != null && !countController.isProceed()) {
            ResultAction resultAction = new ResultAction();
            resultAction.setProperty(TestElement.TEST_CLASS, ResultAction.class.getName());
            resultAction.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ResultActionGui"));
            resultAction.setName("ResultAction");
            resultAction.setProperty("OnError.action", "1000");
            groupTree.add(resultAction);
        }
        // 循环间隔时长设置
        ConstantTimer constantTimer = getConstantTimer();
        if (constantTimer != null) {
            groupTree.add(constantTimer);
        }

        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.toHashTree(groupTree, el.getHashTree(), config);
            });
        }
    }

    private CounterConfig addCounterConfig() {
        CounterConfig counterConfig = new CounterConfig();
        counterConfig.setVarName("MS_LOOP_CONTROLLER_CONFIG");
        counterConfig.setName("数循结果统计计数器");
        counterConfig.setEnabled(true);
        counterConfig.setProperty(TestElement.TEST_CLASS, CounterConfig.class.getName());
        counterConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("CounterConfigGui"));
        counterConfig.setStart(1L);
        counterConfig.setIncrement(1L);
        return counterConfig;
    }

    private LoopController initLoopController() {
        LoopController loopController = new LoopController();
        loopController.setEnabled(this.isEnable());
        loopController.setName("LoopController");
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("LoopControlPanel"));
        loopController.setLoops(countController.getLoops());
        if (countController.getLoops() > 0) {
            loopController.setContinueForever(true);
        }
        return loopController;
    }

    private String getCondition() {
        String variable = "\"" + this.whileController.getVariable() + "\"";
        String operator = this.whileController.getOperator();
        String value;
        if (StringUtils.equals(operator, "<") || StringUtils.equals(operator, ">") && StringUtils.isNumeric(this.whileController.getValue())) {
            value = this.whileController.getValue();
        } else {
            value = "\"" + this.whileController.getValue() + "\"";
        }

        if (StringUtils.contains(operator, "~")) {
            value = "\"(\\n|.)*" + this.whileController.getValue() + "(\\n|.)*\"";
        }

        if (StringUtils.equals(operator, "is empty")) {
            variable = "(" + variable + "==" + "\"\\" + this.whileController.getVariable() + "\"" + "|| empty(" + variable + "))";
            operator = "";
            value = "";
        }

        if (StringUtils.equals(operator, "is not empty")) {
            variable = "(" + variable + "!=" + "\"\\" + this.whileController.getVariable() + "\"" + "&& !empty(" + variable + "))";
            operator = "";
            value = "";
        }
        ms_current_timer = UUID.randomUUID().toString();
        return variable + operator + value;
    }

    private IfController ifController(String condition) {
        IfController ifController = new IfController();
        ifController.setEnabled(this.isEnable());
        ifController.setName("while ifController");
        ifController.setProperty(TestElement.TEST_CLASS, IfController.class.getName());
        ifController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("IfControllerPanel"));
        ifController.setCondition(condition);
        ifController.setEvaluateAll(false);
        ifController.setUseExpression(true);
        return ifController;
    }

    private WhileController initWhileController(String condition) {
        if (StringUtils.isEmpty(condition)) {
            return null;
        }
        WhileController controller = new WhileController();
        controller.setEnabled(this.isEnable());
        controller.setName("WhileController");
        controller.setProperty(TestElement.TEST_CLASS, WhileController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("WhileControllerGui"));
        controller.setCondition(condition);
        return controller;
    }

    private ForeachController initForeachController() {
        ForeachController controller = new ForeachController();
        controller.setEnabled(this.isEnable());
        controller.setName("ForeachController");
        controller.setProperty(TestElement.TEST_CLASS, ForeachController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ForeachControlPanel"));
        controller.setInputVal(this.forEachController.getInputVal());
        controller.setReturnVal(this.forEachController.getReturnVal());
        controller.setUseSeparator(true);
        return controller;
    }

    private String script() {
        String script = "\n" +
                "import java.util.*;\n" +
                "import java.text.SimpleDateFormat;\n" +
                "import org.apache.jmeter.threads.JMeterContextService;\n" +
                "\n" +
                "// 循环控制器超时后结束循环\n" +
                "try{\n" +
                "\tString ms_current_timer = vars.get(\"" + ms_current_timer + "\");\n" +
                "\tlong _nowTime = System.currentTimeMillis(); \n" +
                "\tif(ms_current_timer == null ){\n" +
                "\t\tvars.put(\"" + ms_current_timer + "\",_nowTime.toString());\n" +
                "\t}\n" +
                "\tlong time = Long.parseLong(vars.get(\"" + ms_current_timer + "\"));\n" +
                "\t if((_nowTime - time) > " + this.whileController.getTimeout() + " ){\n" +
                "\t \tvars.put(\"" + ms_current_timer + "\", \"stop\");\n" +
                "\t \tlog.info( \"结束循环\");\n" +
                "\t }\n" +
                "}catch (Exception e){\n" +
                "\tlog.info( e.getMessage());\n" +
                "\tvars.put(\"" + ms_current_timer + "\", \"stop\");\n" +
                "}\n";

        return script;
    }

    private HashTree controller(HashTree tree) {
        if (StringUtils.equals(this.loopType, LoopConstants.WHILE.name()) && this.whileController != null) {
            RunTime runTime = new RunTime();
            runTime.setEnabled(true);
            runTime.setProperty(TestElement.TEST_CLASS, RunTime.class.getName());
            runTime.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("RunTimeGui"));
            long timeout = this.whileController.getTimeout() / 1000;
            if (timeout < 1) {
                timeout = 1;
            }
            runTime.setRuntime(timeout);

            String condition = getCondition();
            String ifCondition = "${__jexl3(" + condition + ")}";
            String whileCondition = "${__jexl3(" + condition + " && \"${" + ms_current_timer + "}\" !=\"stop\")}";
            HashTree ifHashTree = tree.add(ifController(ifCondition));
            HashTree hashTree = ifHashTree.add(initWhileController(whileCondition));
            // 添加超时处理，防止死循环
            JSR223PreProcessor jsr223PreProcessor = new JSR223PreProcessor();
            jsr223PreProcessor.setName("循环超时处理");
            jsr223PreProcessor.setProperty(TestElement.TEST_CLASS, JSR223Sampler.class.getName());
            jsr223PreProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
            /*jsr223PreProcessor.setProperty("cacheKey", "true");*/
            jsr223PreProcessor.setProperty("scriptLanguage", "beanshell");

            ScriptFilter.verify("beanshell", this.getName(), script());

            jsr223PreProcessor.setProperty("script", script());
            hashTree.add(jsr223PreProcessor);
            return hashTree;
        }
        if (StringUtils.equals(this.loopType, LoopConstants.FOREACH.name()) && this.forEachController != null) {
            return tree.add(initForeachController());
        }
        if (StringUtils.equals(this.loopType, LoopConstants.LOOP_COUNT.name()) && this.countController != null) {
            return tree.add(initLoopController());
        }
        return null;
    }

    private ConstantTimer getConstantTimer() {
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(this.isEnable());
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ConstantTimerGui"));
        if (StringUtils.equals(this.loopType, LoopConstants.WHILE.name()) && this.whileController != null) {
            return null;
        }
        if (StringUtils.equals(this.loopType, LoopConstants.FOREACH.name()) && this.forEachController != null) {
            constantTimer.setProperty("ConstantTimer.delay", this.forEachController.getInterval());
            constantTimer.setDelay(this.forEachController.getInterval());
            constantTimer.setName(this.forEachController.getInterval() + " ms");
            return constantTimer;
        }
        if (StringUtils.equals(this.loopType, LoopConstants.LOOP_COUNT.name()) && this.countController != null) {
            constantTimer.setProperty("ConstantTimer.delay", this.countController.getInterval() + "");
            constantTimer.setDelay(this.countController.getInterval() + "");
            constantTimer.setName(this.countController.getInterval() + " ms");
            return constantTimer;
        }
        return null;
    }

}
