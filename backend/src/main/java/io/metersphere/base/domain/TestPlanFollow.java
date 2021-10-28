package io.metersphere.base.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class TestPlanFollow implements Serializable {
    private String testPlanId;

    private String followId;

    private static final long serialVersionUID = 1L;
}