package io.metersphere.api.dto;

import io.metersphere.request.ProjectRequest;
import lombok.Data;

import java.util.List;

@Data
public class ApiProjectRequest extends ProjectRequest {
    private List<String> workspaceIds;

}
