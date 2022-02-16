package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ProjectApplication implements Serializable {
    private String projectId;

    private String type;

    private String typeValue;

    private static final long serialVersionUID = 1L;
}