package io.metersphere.api.parser.jmeter;

import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.testelement.TestElement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-03-05  21:06
 */
public class JmeterTestElementParserHelper {

    public static UserParameters getUserParameters(String name) {
        UserParameters userParameters = new UserParameters();
        userParameters.setEnabled(true);
        userParameters.setName(name + "_User Parameters");
        userParameters.setPerIteration(true);
        userParameters.setProperty(TestElement.TEST_CLASS, UserParameters.class.getName());
        userParameters.setProperty(TestElement.GUI_CLASS, JmeterAlias.USER_PARAMETERS_GUI);
        return userParameters;
    }

    public static UserParameters getUserParameters(String name, List<? extends KeyValueParam> argumentList) {
        UserParameters arguments = getUserParameters(name);
        List<String> names = new LinkedList<>();
        List<Object> values = new LinkedList<>();
        List<Object> threadValues = new LinkedList<>();
        for (int i = 0; i < argumentList.size(); ++i) {
            String value = argumentList.get(i).getValue();
            String key = argumentList.get(i).getKey();
            names.add(key);
            values.add(Mock.buildFunctionCallString(value).replaceAll("[\r\n]", ""));
        }
        arguments.setNames(names);
        threadValues.add(values);
        arguments.setThreadLists(threadValues);
        return arguments;
    }

    public static UserParameters getUserParameters(List<CommonVariables> constantVariables, List<CommonVariables> listVariables) {
        List<CommonVariables> variableResults = new ArrayList<>();
        listVariables.forEach(listVariable -> {
            // 按 ',' 分割，但是支持 '\,' 转义
            String[] arrays = listVariable.getValue().replaceAll("[\r\n]", "").split("(?<!\\\\),");
            for (int i = 0; i < arrays.length; i++) {
                CommonVariables commonVariables = BeanUtils.copyBean(new CommonVariables(), listVariable);
                commonVariables.setKey(listVariable.getKey() + "_" + (i + 1));
                commonVariables.setValue(arrays[i].replace("\\,", ","));
                variableResults.add(commonVariables);
            }
        });
        variableResults.addAll(constantVariables);
        return getUserParameters(StringUtils.EMPTY, variableResults);
    }
}
