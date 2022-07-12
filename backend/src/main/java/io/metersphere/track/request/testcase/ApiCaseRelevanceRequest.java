package io.metersphere.track.request.testcase;

import io.metersphere.api.dto.automation.ApiScenarioRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApiCaseRelevanceRequest {
    /**
     * 测试计划ID
     */
    private String planId;

    private String environmentId;

    /**
     * 具体要关联的用例
     */
    private List<String> selectIds = new ArrayList<>();

    /**
     * 项目环境对应关系
     */
    private Map<String, String> envMap;

    /**
     * 用例的环境的对应关系
     */
    private Map<String, List<String>> mapping;
    /**
     *测试评审ID
     */
    private String reviewId;

    private String environmentType;

    private String envGroupId;

    private List<String> ids;

    private ApiScenarioRequest condition;
}
