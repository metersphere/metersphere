package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProjectApplication implements Serializable {
    private String projectId;

    private String type;

    private String typeValue;

    private static final long serialVersionUID = 1L;
}