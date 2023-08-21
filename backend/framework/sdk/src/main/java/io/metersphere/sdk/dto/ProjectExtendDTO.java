package io.metersphere.sdk.dto;

import io.metersphere.project.domain.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectExtendDTO extends Project implements Serializable {
    private List<String> moduleIds;

    private static final long serialVersionUID = 1L;
}
