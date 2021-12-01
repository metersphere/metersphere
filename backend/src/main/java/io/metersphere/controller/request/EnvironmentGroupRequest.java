package io.metersphere.controller.request;

import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.base.domain.EnvironmentGroupProject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EnvironmentGroupRequest extends EnvironmentGroup {

    private List<EnvironmentGroupProject> envGroupProject;

    /**
     * projectId, envId
     */
    private Map<String, String> map;
    private List<String> groupIds;
    private List<String> projectIds;
}
