package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReportTemplate implements Serializable {
    private String id;

    private String name;

    private String workspaceId;

    private String createUser;

    private String content;

    private static final long serialVersionUID = 1L;
}