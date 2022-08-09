package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IssueTemplateCopyDTO {

    private String workspaceName;

    private List<ProjectDTO> projectDTOS;
}
