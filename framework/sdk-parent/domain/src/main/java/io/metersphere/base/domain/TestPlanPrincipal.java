package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanPrincipal implements Serializable {
    private String testPlanId;

    private String principalId;

    private static final long serialVersionUID = 1L;
}