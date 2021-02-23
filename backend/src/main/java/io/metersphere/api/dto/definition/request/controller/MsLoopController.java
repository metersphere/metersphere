package io.metersphere.api.dto.definition.request.controller;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.controller.loop.CountController;
import io.metersphere.api.dto.definition.request.controller.loop.MsForEachController;
import io.metersphere.api.dto.definition.request.controller.loop.MsWhileController;
import io.metersphere.commons.constants.LoopConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.RunTime;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.reporters.ResultAction;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "LoopController")
public class MsLoopController extends MsTestElement {
    private String type = "LoopController";
    @JSONField(ordinal = 20)
    private String loopType;

    @JSONField(ordinal = 21)
    private CountController countController;

    @JSONField(ordinal = 22)
    private MsForEachController forEachController;

    @JSONField(ordinal = 23)
    private MsWhileController whileController;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        final HashTree groupTree = controller(tree);
        if (CollectionUtils.isNotEmpty(config.getVariables())) {
            this.addCsvDataSet(groupTree, config.getVariables());
            this.addCounter(groupTree, config.getVariables());
            this.addRandom(groupTree, config.getVariables());
        }

        // 循环下都增加一个计数器，用于结果统计
        groupTree.add(addCounterConfig());
        // 不打开执行成功后轮询功能，则成功后就停止循环
        if (StringUtils.equals(this.loopType, LoopConstants.LOOP_COUNT.name()) && this.countController != null && !countController.isProceed()) {
            ResultAction resultAction = new ResultAction();
            resultAction.setName("ResultAction");
            resultAction.setProperty("OnError.action", "1000");
            groupTree.add(resultAction);
        }
        // 循环间隔时长设置
        ConstantTimer cnstantTimer = getCnstantTimer();
        if (cnstantTimer != null) {
            groupTree.add(cnstantTimer);
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
        counterConfig.setVarName("LoopCounterConfigXXX");
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
        loopController.setContinueForever(false);
        return loopController;
    }

    private String getCondition() {
        String variable = "\"" + this.whileController.getVariable() + "\"";
        String operator = this.whileController.getOperator();
        String value = "\"" + this.whileController.getValue() + "\"";

        if (StringUtils.contains(operator, "~")) {
            value = "\".*" + this.whileController.getValue() + ".*\"";
        }

        if (StringUtils.equals(operator, "is empty")) {
            variable = variable + "==" + "\"\\" + this.whileController.getVariable() + "\"" + "|| empty(" + variable + ")";
            operator = "";
            value = "";
        }

        if (StringUtils.equals(operator, "is not empty")) {
            variable = variable + "!=" + "\"\\" + this.whileController.getVariable() + "\"" + "&& !empty(" + variable + ")";
            operator = "";
            value = "";
        }
        return "${__jexl3(" + variable + operator + value + ")}";
    }

    private WhileController initWhileController() {
        String condition = getCondition();
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
            // 添加超时处理，防止死循环
            HashTree hashTree = tree.add(runTime);
            return hashTree.add(initWhileController());
        }
        if (StringUtils.equals(this.loopType, LoopConstants.FOREACH.name()) && this.forEachController != null) {
            return tree.add(initForeachController());
        }
        if (StringUtils.equals(this.loopType, LoopConstants.LOOP_COUNT.name()) && this.countController != null) {
            return tree.add(initLoopController());
        }
        return null;
    }

    private ConstantTimer getCnstantTimer() {
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
