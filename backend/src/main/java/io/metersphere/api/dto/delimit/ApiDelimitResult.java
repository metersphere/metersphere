package io.metersphere.api.dto.delimit;

import io.metersphere.base.domain.ApiDelimit;
import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiDelimitResult extends ApiDelimit {

    private String projectName;

    private String userName;

    private String caseTotal;

    private String caseStatus;

    private String casePassingRate;

    private Schedule schedule;
}
