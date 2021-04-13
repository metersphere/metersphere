package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseFieldTemplate implements Serializable {
    private String id;

    private String name;

    private String type;

    private String description;

    private String caseName;

    private String workspaceId;

    private String prerequisite;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}