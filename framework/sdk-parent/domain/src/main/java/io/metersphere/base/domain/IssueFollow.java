package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class IssueFollow implements Serializable {
    private String issueId;

    private String followId;

    private static final long serialVersionUID = 1L;
}