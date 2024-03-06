package io.metersphere.api.parser.jmeter;

import io.metersphere.jmeter.mock.Mock;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;

import java.util.ArrayList;
import java.util.List;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.ARGUMENTS_PANEL;

/**
 * @Author: jianxing
 * @CreateTime: 2024-03-05  21:06
 */
public class JmeterTestElementParserHelper {

    public static Arguments getArguments(String name) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(name + "_Arguments");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(ARGUMENTS_PANEL));
        return arguments;
    }

    public static Arguments getArguments(String name, List<CommonVariables> argumentList) {
        Arguments arguments = getArguments(name);
        parse2ArgumentList(argumentList).forEach(arguments::addArgument);
        return arguments;
    }

    public static List<Argument> parse2ArgumentList(List<CommonVariables> variables) {
        List<Argument> arguments = new ArrayList<>(variables.size());
        variables.forEach(variable -> {
            if (BooleanUtils.isFalse(variable.getEnable())) {
                return;
            }
            if (variable.isConstantValid()) {
                // 处理常量
                String value = StringUtils.isBlank(variable.getValue()) ? variable.getValue()
                        : Mock.buildFunctionCallString(variable.getValue()).replaceAll("[\r\n]", "");
                arguments.add(new Argument(variable.getKey(), value, "="));
            } else if (variable.isListValid()) {
                // 处理 List 变量
                String[] arrays = variable.getValue().replaceAll("[\r\n]", "").split(",");
                for (int i = 0; i < arrays.length; i++) {
                    arguments.add(new Argument(variable.getKey() + "_" + (i + 1), arrays[i], "="));
                }
            }
        });
        return arguments;
    }


}
