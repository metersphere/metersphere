package io.metersphere.api.parser.jmeter.controller;

import io.metersphere.api.dto.request.controller.LoopType;
import io.metersphere.api.dto.request.controller.MsLoopController;
import io.metersphere.api.dto.request.controller.WhileConditionType;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.*;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

public class MsLoopControllerConverter extends AbstractJmeterElementConverter<MsLoopController> {


    @Override
    public void toHashTree(HashTree tree, MsLoopController element, ParameterConfig config) {

        if (BooleanUtils.isFalse(element.getEnable())) {
            LogUtils.info("MsLoopController is disabled");
            return;
        }
        final HashTree groupTree = controller(tree, element);

        if (groupTree == null) {
            return;
        }

        // 循环间隔时长设置
        ConstantTimer constantTimer = getConstantTimer(element);
        if (constantTimer != null) {
            groupTree.add(constantTimer);
        }

        parseChild(groupTree, element, config);

        //如果是while循环，需要添加一个循环控制器超时处理
        if (StringUtils.equals(element.getLoopType(), LoopType.WHILE.name()) && element.getWhileController() != null) {
            String ifCondition = "${__jexl3(${__time(,)} - ${\"" + element.getCurrentTime() + "\"} >" + element.getWhileController().getTimeout() + ")}";
            IfController ifController = ifController(ifCondition, element.getEnable());
            TestAction testAction = new TestAction();
            testAction.setEnabled(true);
            testAction.setName("结束循环");
            testAction.setProperty("ActionProcessor.action", 5);
            testAction.setProperty(TestElement.TEST_CLASS, TestAction.class.getName());
            testAction.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestActionGui"));
            testAction.setProperty("ActionProcessor.target",0);

            groupTree.add(ifController, testAction);
        }

    }


    private HashTree controller(HashTree tree, MsLoopController element) {
        if (StringUtils.equals(element.getLoopType(), LoopType.WHILE.name()) && element.getWhileController() != null) {
            RunTime runTime = new RunTime();
            runTime.setEnabled(true);
            runTime.setProperty(TestElement.TEST_CLASS, RunTime.class.getName());
            runTime.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("RunTimeGui"));
            long timeout = element.getWhileController().getTimeout() / 1000;
            if (timeout < 1) {
                timeout = 1;
            }
            runTime.setRuntime(timeout);
            String condition;
            if (StringUtils.equals(WhileConditionType.CONDITION.name(), element.getWhileController().getConditionType())) {
                condition = element.getWhileController().getMsWhileVariable().getConditionValue();
            } else {
                condition = element.getWhileController().getMsWhileScript().getScriptValue();
            }
            String ifCondition = "${__jexl3(" + condition + ")}";
            HashTree ifHashTree = tree.add(ifController(ifCondition, element.getEnable()));
            addPreProc(ifHashTree, element);
            return ifHashTree.add(initWhileController(element, ifCondition));
        }
        if (StringUtils.equals(element.getLoopType(), LoopType.FOREACH.name()) && element.getForEachController() != null) {
            return tree.add(initForeachController(element));
        }
        if (StringUtils.equals(element.getLoopType(), LoopType.LOOP_COUNT.name()) && element.getMsCountController() != null) {
            String ifCondition = StringUtils.join("${__jexl3(", element.getMsCountController().getLoops(), " > 0 ", ")}");
            HashTree ifHashTree = tree.add(ifController(ifCondition, element.getEnable()));
            return ifHashTree.add(initLoopController(element));
        }
        return null;
    }


    private void addPreProc(HashTree hashTree, MsLoopController element) {
        JSR223Sampler sampler = new JSR223Sampler();
        sampler.setName("MS_CLEAR_LOOPS_VAR_" + element.getCurrentTime());
        sampler.setProperty(TestElement.TEST_CLASS, JSR223Sampler.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        sampler.setProperty("script", "${__time(,\"" + element.getCurrentTime() + "\")};");
        hashTree.add(sampler);
    }

    private LoopController initLoopController(MsLoopController element) {
        LoopController loopController = new LoopController();
        loopController.setEnabled(element.getEnable());
        loopController.setName(StringUtils.isNotBlank(element.getName()) ? element.getName() : "Loop Controller");
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("LoopControlPanel"));
        loopController.setLoops(element.getMsCountController().getLoops());
        return loopController;
    }

    private IfController ifController(String condition, boolean enable) {
        IfController ifController = new IfController();
        ifController.setEnabled(enable);
        ifController.setName("while ifController");
        ifController.setProperty(TestElement.TEST_CLASS, IfController.class.getName());
        ifController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("IfControllerPanel"));
        ifController.setCondition(condition);
        ifController.setEvaluateAll(false);
        ifController.setUseExpression(true);
        return ifController;
    }

    private WhileController initWhileController(MsLoopController element, String condition) {
        WhileController controller = new WhileController();
        controller.setEnabled(element.getEnable());
        controller.setName(StringUtils.isNotBlank(element.getName()) ? element.getName() : "While Controller");
        controller.setProperty(TestElement.TEST_CLASS, WhileController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("WhileControllerGui"));
        controller.setCondition(condition);
        return controller;
    }

    private ForeachController initForeachController(MsLoopController element) {
        ForeachController controller = new ForeachController();
        controller.setEnabled(element.getEnable());
        controller.setName(StringUtils.isNotBlank(element.getName()) ? element.getName() : "Foreach Controller");
        controller.setProperty(TestElement.TEST_CLASS, ForeachController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ForeachControlPanel"));
        controller.setInputVal(element.getForEachController().getValue());
        controller.setReturnVal(element.getForEachController().getVariable());
        controller.setUseSeparator(true);
        return controller;
    }

    private ConstantTimer getConstantTimer(MsLoopController element) {
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(element.getEnable());
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ConstantTimerGui"));
        if (StringUtils.equals(element.getLoopType(), LoopType.WHILE.name()) && element.getWhileController() != null) {
            return null;
        }
        if (StringUtils.equals(element.getLoopType(), LoopType.FOREACH.name()) && element.getForEachController() != null &&
                element.getForEachController().getLoopTime() > 0) {
            constantTimer.setProperty("ConstantTimer.delay", element.getForEachController().getLoopTime());
            constantTimer.setDelay(element.getForEachController().getLoopTime() + StringUtils.EMPTY);
            constantTimer.setName(element.getForEachController().getLoopTime() + " ms");
            return constantTimer;
        }
        if (StringUtils.equals(element.getLoopType(), LoopType.LOOP_COUNT.name()) && element.getMsCountController() != null &&
                element.getMsCountController().getLoopTime() > 0) {
            constantTimer.setProperty("ConstantTimer.delay", element.getMsCountController().getLoopTime() + "");
            constantTimer.setDelay(element.getMsCountController().getLoopTime() + StringUtils.EMPTY);
            constantTimer.setName(element.getMsCountController().getLoopTime() + " ms");
            return constantTimer;
        }
        return null;
    }
}
