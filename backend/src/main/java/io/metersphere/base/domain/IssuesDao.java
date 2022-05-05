package io.metersphere.base.domain;

import io.metersphere.dto.CustomFieldDao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IssuesDao extends IssuesWithBLOBs {
    private String model;
    private String projectName;
    private String creatorName;
    private String resourceName;
    private long caseCount;
    private List<String> caseIds;
    private String caseId;
    private int totalIssueCount;
    private List<String> tapdUsers;
    private List<String>zentaoBuilds;
    private String zentaoAssigned;
    private String refType;
    private String refId;
    private List<CustomFieldDao> fields;
}
