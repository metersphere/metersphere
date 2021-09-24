package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanPrincipal implements Serializable {
    private String testPlanId;

    private String principalId;

    private static final long serialVersionUID = 1L;
}