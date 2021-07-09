package io.metersphere.dto;

import io.metersphere.base.domain.Project;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDTO extends Project {
    private String workspaceName;
    private String createUserName;
}
