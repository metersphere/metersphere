package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseIssues implements Serializable {
    private String id;

    private String resourceId;

    private String issuesId;

    private String refType;

    private String refId;

    private Long relateTime;

    private static final long serialVersionUID = 1L;
}