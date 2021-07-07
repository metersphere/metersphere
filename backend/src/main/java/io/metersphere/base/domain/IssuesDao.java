package io.metersphere.base.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuesDao extends IssuesWithBLOBs {
    private String model;
    private String projectName;
    private String creatorName;
    private String resourceName;
    private long caseCount;
}
