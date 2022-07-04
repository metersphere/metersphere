package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class IssueFile implements Serializable {
    private String issueId;

    private String fileId;

    private static final long serialVersionUID = 1L;
}