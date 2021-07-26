package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiLoadTest implements Serializable {
    private String id;

    private String apiId;

    private String loadTestId;

    private String envId;

    private String type;

    private Integer apiVersion;

    private static final long serialVersionUID = 1L;
}
