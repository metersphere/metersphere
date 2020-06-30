package io.metersphere.excel.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.i18n.Translator;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestCaseDataListener extends EasyExcelListener<TestCaseExcelData> {

    private TestCaseService testCaseService;

    private String projectId;

    Set<String> testCaseNames;

    Set<String> userIds;

    public TestCaseDataListener(TestCaseService testCaseService, String projectId, Set<String> testCaseNames, Set<String> userIds) {
        this.testCaseService = testCaseService;
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
            if ( nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                stringBuilder.append(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"));
            }
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    stringBuilder.append(Translator.get("module_not_null"));
                    break;
                }
            }
        }

        if (!userIds.contains(data.getMaintainer())) {
            stringBuilder.append(Translator.get("user_not_exists") + "：" + data.getMaintainer() + "; ");
        }
        if (testCaseNames.contains(data.getName())) {
            stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + data.getName() + "; ");
        } else {
            testCaseNames.add(data.getName());
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

        for (int i = 0; i < index; i++){

            JSONObject step = new JSONObject();
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

        testCase.setSteps(jsonArray.toJSONString());

        return testCase;
    }

}
