package io.metersphere.plan.utils;

import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.TestPlanApiDTO;
import io.metersphere.dto.TestPlanScenarioDTO;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.ApiPlanReportDTO;
import io.metersphere.service.ServiceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestPlanReportUtil {

    public static Map<String, List<String>> mergeProjectEnvMap(Map<String, List<String>> projectEnvMap, Map<String, List<String>> originProjectEnvMap) {
        if (projectEnvMap == null) {
            projectEnvMap = new HashMap<String, List<String>>();
        }
        if (originProjectEnvMap == null) {
            originProjectEnvMap = new HashMap<String, List<String>>();
        }
        Map<String, List<String>> r = new HashMap<>();
        projectEnvMap.entrySet().forEach(e -> {
            r.put(e.getKey(), e.getValue());
        });
        originProjectEnvMap.entrySet().forEach(e -> {
            if (r.containsKey(e.getKey())) {
                r.get(e.getKey()).addAll(e.getValue());
                r.put(e.getKey(), r.get(e.getKey()).stream().distinct().collect(Collectors.toList()));
            } else {
                r.put(e.getKey(), e.getValue());
            }
        });
        return r;
    }

    //获取测试计划执行时的环境
    public static Map<String, List<String>> getTestPlanExecutedEnvironments(Map<String, List<String>> defaultEnvMap, Map<String, String> selectedEnvMap) {
        Map<String, List<String>> returnMap = new HashMap<String, List<String>>();
        if (MapUtils.isNotEmpty(defaultEnvMap)) {
            returnMap.putAll(defaultEnvMap);
        }
        if (MapUtils.isNotEmpty(selectedEnvMap)) {
            for (Map.Entry<String, String> entry : selectedEnvMap.entrySet()) {
                String projectId = entry.getKey();
                returnMap.put(projectId, new ArrayList<>() {{
                    this.add(entry.getValue());
                }});
            }
        }
        return returnMap;
    }

    public static Map<String, List<String>> mergeEnvironmentMap(Map<String, List<String>> projectEnvMap, Map<String, String> originProjectEnvMap) {
        if (projectEnvMap == null) {
            projectEnvMap = new HashMap<String, List<String>>();
        }
        if (MapUtils.isEmpty(originProjectEnvMap)) {
            return projectEnvMap;
        }
        Map<String, List<String>> r = new HashMap<>();
        projectEnvMap.entrySet().forEach(e -> {
            r.put(e.getKey(), e.getValue());
        });
        originProjectEnvMap.entrySet().forEach(e -> {
            if (r.containsKey(e.getKey())) {
                r.get(e.getKey()).add(e.getValue());
                r.put(e.getKey(), r.get(e.getKey()).stream().distinct().collect(Collectors.toList()));
            } else {
                r.put(e.getKey(), new ArrayList<>() {{
                    this.add(e.getValue());
                }});
            }
        });
        return r;
    }

    public static boolean checkReportConfig(Map config, String key, String subKey) {
        return ServiceUtils.checkConfigEnable(config, key, subKey);
    }

    public static void screenApiCaseByStatusAndReportConfig(ApiPlanReportDTO report, List<TestPlanApiDTO> apiAllCases, Map reportConfig) {
        if (!CollectionUtils.isEmpty(apiAllCases)) {
            List<TestPlanApiDTO> apiFailureCases = new ArrayList<>();
            List<TestPlanApiDTO> apiErrorReportCases = new ArrayList<>();
            List<TestPlanApiDTO> apiUnExecuteCases = new ArrayList<>();
            for (TestPlanApiDTO apiDTO : apiAllCases) {
                if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.ERROR.name())) {
                    apiFailureCases.add(apiDTO);
                } else if (StringUtils.equalsIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.FAKE_ERROR.name())) {
                    apiErrorReportCases.add(apiDTO);
                } else if (StringUtils.equalsAnyIgnoreCase(apiDTO.getExecResult(), ApiReportStatus.STOPPED.name(),
                        ApiReportStatus.PENDING.name())) {
                    apiUnExecuteCases.add(apiDTO);
                }
            }

            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setApiFailureCases(apiFailureCases);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.FAKE_ERROR.name())) {
                report.setErrorReportCases(apiErrorReportCases);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.PENDING.name())) {
                report.setUnExecuteCases(apiUnExecuteCases);
            }
        }
    }

    public static void screenScenariosByStatusAndReportConfig(ApiPlanReportDTO report, List<TestPlanScenarioDTO> scenarios, Map reportConfig) {
        if (!CollectionUtils.isEmpty(scenarios)) {
            List<TestPlanScenarioDTO> failureScenarios = new ArrayList<>();
            List<TestPlanScenarioDTO> errorReportScenarios = new ArrayList<>();
            List<TestPlanScenarioDTO> unExecuteScenarios = new ArrayList<>();
            for (TestPlanScenarioDTO scenario : scenarios) {
                if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), ApiReportStatus.ERROR.name())) {
                    failureScenarios.add(scenario);
                } else if (StringUtils.equalsIgnoreCase(scenario.getLastResult(), ApiReportStatus.FAKE_ERROR.name())) {
                    errorReportScenarios.add(scenario);
                } else if (StringUtils.equalsAnyIgnoreCase(scenario.getLastResult(), ApiReportStatus.STOPPED.name(),
                        ApiReportStatus.PENDING.name())) {
                    unExecuteScenarios.add(scenario);
                }
            }
            if (checkReportConfig(reportConfig, "api", "failure")) {
                report.setScenarioFailureCases(failureScenarios);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.FAKE_ERROR.name())) {
                report.setErrorReportScenarios(errorReportScenarios);
            }
            if (checkReportConfig(reportConfig, "api", ApiReportStatus.PENDING.name())) {
                report.setUnExecuteScenarios(unExecuteScenarios);
            }
        }
    }

    private static <T> T getReportContentResultObject(String contentStr, Class<T> clazz) {
        if (StringUtils.isNotBlank(contentStr)) {
            return JSON.parseObject(contentStr, clazz);
        }
        return null;
    }

    private static <T> List<T> getReportContentResultArray(String contentStr, Class<T> clazz) {
        if (StringUtils.isNotBlank(contentStr)) {
            return JSON.parseArray(contentStr, clazz);
        }
        return null;
    }

    public static List<String> mergeResourcePools(List<String> resourcePools, List<String> reFormatOriginResourcePools) {
        if (resourcePools == null) {
            resourcePools = new ArrayList<>();
        }
        if (reFormatOriginResourcePools == null) {
            return resourcePools;
        }
        //检查originResourcePools是否含有null。 null代表local
        List<String> originResourcePools = new ArrayList<>();
        for (String resourcePool : reFormatOriginResourcePools) {
            if (StringUtils.isEmpty(resourcePool)) {
                resourcePool = "LOCAL";
            }
            if (!originResourcePools.contains(resourcePool)) {
                originResourcePools.add(resourcePool);
            }
        }
        List<String> returnList = new ArrayList<>();
        returnList.addAll(resourcePools);
        returnList.addAll(originResourcePools);
        return returnList.stream().distinct().collect(Collectors.toList());
    }
}
