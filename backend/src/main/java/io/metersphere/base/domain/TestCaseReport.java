package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReport implements Serializable {
    private String id;

    private String name;

    private Long startTime;

    private Long endTime;

    private String createUser;

    private String content;

    private static final long serialVersionUID = 1L;
}