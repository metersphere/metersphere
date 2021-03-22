package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseTest implements Serializable {
    private String id;

    private String testCaseId;

    private String testId;

    private String testType;

    private static final long serialVersionUID = 1L;
}