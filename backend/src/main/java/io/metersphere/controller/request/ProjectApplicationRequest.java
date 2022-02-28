package io.metersphere.controller.request;

import io.metersphere.base.domain.ProjectApplication;
import lombok.Data;

import java.util.List;

@Data
public class ProjectApplicationRequest {
    private List<ProjectApplication> configs;
}
