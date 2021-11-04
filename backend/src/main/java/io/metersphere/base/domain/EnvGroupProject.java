package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class EnvGroupProject implements Serializable {
    private String envGroupId;

    private String projectId;

    private String envId;

    private String description;

    private String createUser;

    private static final long serialVersionUID = 1L;
}