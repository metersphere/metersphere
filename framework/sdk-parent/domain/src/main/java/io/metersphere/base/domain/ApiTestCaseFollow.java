package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTestCaseFollow implements Serializable {
    private String caseId;

    private String followId;

    private static final long serialVersionUID = 1L;
}