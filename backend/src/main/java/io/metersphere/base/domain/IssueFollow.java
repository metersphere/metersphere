package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class IssueFollow implements Serializable {
    private String issueId;

    private String followId;

    private static final long serialVersionUID = 1L;
}