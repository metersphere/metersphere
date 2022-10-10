package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class EnvironmentGroupProject implements Serializable {
    private String id;

    private String environmentGroupId;

    private String environmentId;

    private String projectId;

    private static final long serialVersionUID = 1L;
}