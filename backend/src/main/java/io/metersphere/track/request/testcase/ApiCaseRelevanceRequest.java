package io.metersphere.track.request.testcase;

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
     * 当选择关联全部用例时把加载条件送到后台，从后台查询
     */
//    private QueryTestCaseRequest request;

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
}
