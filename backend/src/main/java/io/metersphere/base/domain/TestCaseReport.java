package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReport implements Serializable {
    private Long id;

    private String name;

    private String planId;

    private Long startTime;

    private Long endTime;

    private String content;

    private static final long serialVersionUID = 1L;
}