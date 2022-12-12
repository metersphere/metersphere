package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ProjectVersionRequest {
    private String name;
    private String projectId;
    private String createUser;
    private Boolean latest;
    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;
    private Map<String, Object> combine;
    private String type;
    private String versionId;
    private String resourceId;

}
