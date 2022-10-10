package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiSyncRuleRelationRequest {
    private String resourceId;

    private String resourceType;

    private Boolean showUpdateRule;

    private boolean caseCreator;

    private boolean scenarioCreator;

    private boolean syncCase;

    private boolean sendNotice;

    private String apiSyncCaseRequest;
}
