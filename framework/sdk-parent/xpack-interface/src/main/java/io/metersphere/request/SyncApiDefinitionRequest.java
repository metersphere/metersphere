package io.metersphere.request;

import io.metersphere.plugin.core.MsTestElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SyncApiDefinitionRequest {
    private String id;

    private String projectId;

    private String protocol;

    private MsTestElement request;

    //是否进入待更新列表
    private Boolean toBeUpdated;

    public SyncApiDefinitionRequest() {
    }

    public SyncApiDefinitionRequest(String id, String projectId, String protocol, MsTestElement request, Boolean toBeUpdated) {
        this.id = id;
        this.projectId = projectId;
        this.protocol = protocol;
        this.request = request;
        this.toBeUpdated = toBeUpdated;
    }
}
