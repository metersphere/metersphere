package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class IssueFile implements Serializable {
    private String issueId;

    private String fileId;

    private static final long serialVersionUID = 1L;
}