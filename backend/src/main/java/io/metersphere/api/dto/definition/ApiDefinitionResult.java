package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiDefinitionResult extends ApiDefinitionWithBLOBs {

    private String projectName;

    private String userName;

    private String caseTotal;

    private String caseStatus;

    private String scenarioTotal;

    private String casePassingRate;

    private String deleteUser;

    private String[] ids;

    private String caseType;

    private String scenarioType;

    private String apiType;

    private String versionName;

    private Boolean versionEnable;
}
