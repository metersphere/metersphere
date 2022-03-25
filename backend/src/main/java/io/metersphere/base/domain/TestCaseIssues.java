package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseIssues implements Serializable {
    private String id;

    private String resourceId;

    private String issuesId;

    private String refId;

    private String refType;

    private static final long serialVersionUID = 1L;
}
