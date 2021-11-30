package io.metersphere.controller.request;

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
    private List<OrderRequest> orders;
    private Map<String, List<String>> filters;
    private Map<String, Object> combine;
}
