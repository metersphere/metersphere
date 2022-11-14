package io.metersphere.commons.utils;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PerformInspectionUtil {
    private static final List<String> runPaths = List.of(
            "/api/testcase/batch/run", "/api/testcase/jenkins/run",
            "/api/definition/run", "/api/definition/run/debug",
            "/api/automation/run", "/api/automation/run/debug", "/api/automation/jenkins/run");

    public static void countMatches(String content, String checkItem) {
        if (StringUtils.isNotBlank(content) && StringUtils.isNotBlank(checkItem) && isPathMatches(content)
                && StringUtils.contains(content, checkItem) && StringUtils.countMatches(content, checkItem) > 1) {
            LoggerUtil.error("执行内容含自身信息", content, checkItem);
            MSException.throwException("执行内容不能包含自身接口信息");
        }
    }

    public static void inspection(String content, String checkItem, int size) {
        if (StringUtils.isNotBlank(content) && StringUtils.isNotBlank(checkItem) && isPathMatches(content)
                && StringUtils.contains(content, checkItem) && StringUtils.countMatches(content, checkItem) > size) {
            LoggerUtil.error("执行内容含自身信息", content, checkItem);
            MSException.throwException("执行内容不能包含自身接口信息");
        }
    }

    public static void scenarioInspection(List<ApiScenarioWithBLOBs> scenarios) {
        if (CollectionUtils.isNotEmpty(scenarios)) {
            scenarios.forEach(scenario -> {
                countMatches(scenario.getScenarioDefinition(), scenario.getId());
            });
        }
    }

    public static void caseInspection(List<ApiTestCaseWithBLOBs> caseList) {
        if (CollectionUtils.isNotEmpty(caseList)) {
            caseList.forEach(item -> {
                inspection(item.getRequest(), item.getId(), 2);
            });
        }
    }

    private static boolean isPathMatches(String content) {
        return runPaths.stream().filter(path -> StringUtils.contains(content, path)).count() > 0;
    }
}
