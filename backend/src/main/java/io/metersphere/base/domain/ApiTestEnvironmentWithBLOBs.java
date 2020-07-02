package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiTestEnvironmentWithBLOBs extends ApiTestEnvironment implements Serializable {
    private String variables;

    private String headers;

    private String customData;

    private static final long serialVersionUID = 1L;
}