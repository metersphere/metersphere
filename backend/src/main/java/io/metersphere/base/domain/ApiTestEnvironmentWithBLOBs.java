package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiTestEnvironmentWithBLOBs extends ApiTestEnvironment implements Serializable {
    private String variables;

    private String headers;

    private String config;

    private String hosts;

    private static final long serialVersionUID = 1L;
}