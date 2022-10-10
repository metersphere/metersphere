package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanFollow implements Serializable {
    private String testPlanId;

    private String followId;

    private static final long serialVersionUID = 1L;
}