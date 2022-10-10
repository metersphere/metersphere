package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseFile implements Serializable {
    private String caseId;

    private String fileId;

    private static final long serialVersionUID = 1L;
}