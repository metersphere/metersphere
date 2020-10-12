package io.metersphere.excel.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.i18n.Translator;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestCaseDataListener extends EasyExcelListener<TestCaseExcelData> {

    private TestCaseService testCaseService;

    private String projectId;

    Set<String> testCaseNames;

    Set<String> userIds;

    public TestCaseDataListener(Class clazz, String projectId, Set<String> testCaseNames, Set<String> userIds) {
        this.clazz = clazz;
        this.testCaseService = (TestCaseService) CommonBeanFactory.getBean("testCaseService");
        this.projectId = projectId;
        this.testCaseNames = testCaseNames;
        this.userIds = userIds;
    }

    @Override
    public String validate(TestCaseExcelData data, String errMsg) {
        String nodePath = data.getNodePath();
        StringBuilder stringBuilder = new StringBuilder(errMsg);

        if (nodePath != null) {
            String[] nodes = nodePath.split("/");
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                stringBuilder.append(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level") + "; ");
            }
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    stringBuilder.append(Translator.get("module_not_null") + "; ");
                    break;
                }
            }
        }

        if (StringUtils.equals(data.getType(), TestCaseConstants.Type.Functional.getValue()) && StringUtils.equals(data.getMethod(), TestCaseConstants.Method.Auto.getValue())) {
            stringBuilder.append(Translator.get("functional_method_tip") + "; ");
        }

        if (!userIds.contains(data.getMaintainer())) {
            stringBuilder.append(Translator.get("user_not_exists") + "：" + data.getMaintainer() + "; ");
        }

        if (testCaseNames.contains(data.getName())) {
            TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
            BeanUtils.copyBean(testCase, data);
            testCase.setProjectId(projectId);
            String steps = getSteps(data);
            testCase.setSteps(steps);

            boolean dbExist = testCaseService.exist(testCase);
            boolean excelExist = false;

            if (dbExist) {
                // db exist
                stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + data.getName() + "; ");
            } else {
                // @Data 重写了 equals 和 hashCode 方法
                excelExist = excelDataList.contains(data);
            }

            if (excelExist) {
                // excel exist
                stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + data.getName() + "; ");
            } else {
                excelDataList.add(data);
            }

        } else {
            testCaseNames.add(data.getName());
            excelDataList.add(data);
        }
        return stringBuilder.toString();
    }

    @Override
    public void saveData() {

        //无错误数据才插入数据
        if (!errList.isEmpty()) {
            return;
        }

        Collections.reverse(list);

        List<TestCaseWithBLOBs> result = list.stream()
                .map(item -> this.convert2TestCase(item))
                .collect(Collectors.toList());

        testCaseService.saveImportData(result, projectId);

    }


    private TestCaseWithBLOBs convert2TestCase(TestCaseExcelData data) {
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        BeanUtils.copyBean(testCase, data);
        testCase.setId(UUID.randomUUID().toString());
        testCase.setProjectId(this.projectId);
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        String nodePath = data.getNodePath();

        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }

        testCase.setNodePath(nodePath);


        String steps = getSteps(data);
        testCase.setSteps(steps);

        return testCase;
    }

    public String getSteps(TestCaseExcelData data) {
        JSONArray jsonArray = new JSONArray();

        String[] stepDesc = new String[1];
        String[] stepRes = new String[1];

        if (data.getStepDesc() != null) {
            stepDesc = data.getStepDesc().split("\n");
        } else {
            stepDesc[0] = "";
        }
        if (data.getStepResult() != null) {
            stepRes = data.getStepResult().split("\n");
        } else {
            stepRes[0] = "";
        }

        String pattern = "(^\\d+)(\\.)?";
        int index = stepDesc.length > stepRes.length ? stepDesc.length : stepRes.length;

        for (int i = 0; i < index; i++) {

            // 保持插入顺序，判断用例是否有相同的steps
            JSONObject step = new JSONObject(true);
            step.put("num", i + 1);

            Pattern descPattern = Pattern.compile(pattern);
            Pattern resPattern = Pattern.compile(pattern);

            if (i < stepDesc.length) {
                Matcher descMatcher = descPattern.matcher(stepDesc[i]);
                if (descMatcher.find()) {
                    step.put("desc", descMatcher.replaceAll(""));
                } else {
                    step.put("desc", stepDesc[i]);
                }
            }

            if (i < stepRes.length) {
                Matcher resMatcher = resPattern.matcher(stepRes[i]);
                if (resMatcher.find()) {
                    step.put("result", resMatcher.replaceAll(""));
                } else {
                    step.put("result", stepRes[i]);
                }
            }

            jsonArray.add(step);
        }
        return jsonArray.toJSONString();
    }

}
