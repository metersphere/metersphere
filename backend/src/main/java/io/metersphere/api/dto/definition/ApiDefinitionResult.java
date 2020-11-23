package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiDefinitionResult extends ApiDefinition {

    private String projectName;

    private String url;

    private String userName;

    private String caseTotal;

    private String caseStatus;

    private String casePassingRate;

    private Schedule schedule;
}
