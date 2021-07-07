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

    private String casePassingRate;

    private String deleteUser;
}
