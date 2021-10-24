package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiDefinitionFollow implements Serializable {
    private String definitionId;

    private String followId;

    private static final long serialVersionUID = 1L;
}