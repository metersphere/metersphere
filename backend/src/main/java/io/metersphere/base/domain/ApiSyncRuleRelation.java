package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiSyncRuleRelation implements Serializable {
    private String id;

    private String resourceId;

    private String resourceType;

    private Boolean showUpdateRule;

    private Boolean caseCreator;

    private Boolean scenarioCreator;

    private Boolean syncCase;

    private Boolean sendNotice;

    private String apiSyncCaseRequest;

    private static final long serialVersionUID = 1L;
}