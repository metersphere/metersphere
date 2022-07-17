package io.metersphere.dto;

import io.metersphere.base.domain.UiScenarioWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UiScenarioDTO extends UiScenarioWithBLOBs {

    private String projectName;
    private String userName;
    private String creatorName;
    private String principalName;
    private List<String> tagNames;
    private String deleteUser;
    private String versionName;
    private Boolean versionEnable;

    /**
     * 场景跨项目ID
     */
    private List<String> projectIds;

    private String caseId;
    private String environment;
    /**
     * 场景列表 环境
     */
    private String env;
    private Map<String, String> environmentMap;
    private String creator;
}
