package io.metersphere.controller.request;

import io.metersphere.base.domain.Project;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProjectRequest extends Project {
    private String protocal;
}
