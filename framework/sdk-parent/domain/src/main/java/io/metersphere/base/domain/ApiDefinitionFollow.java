package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionFollow implements Serializable {
    private String definitionId;

    private String followId;

    private static final long serialVersionUID = 1L;
}