package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class IssueComment implements Serializable {
    private String id;

    private String issueId;

    private String author;

    private Long createTime;

    private Long updateTime;

    private String status;

    private String description;

    private static final long serialVersionUID = 1L;
}