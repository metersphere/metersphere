package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class EnvGroup implements Serializable {
    private String envGroupId;

    private String envGroupName;

    private static final long serialVersionUID = 1L;
}