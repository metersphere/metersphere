package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanProject implements Serializable {
    private String testPlanId;

    private String projectId;

    private static final long serialVersionUID = 1L;
}