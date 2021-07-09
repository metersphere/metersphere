package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RelevanceScenarioRequest {

    private List<String> ids;
    /**
     * 环境和项目对应关系
     */
    private Map<String, String> envMap;

    /**
     * 场景用例跨项目的关系
     */
    private Map<String, List<String>> mapping;

}
