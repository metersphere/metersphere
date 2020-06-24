package io.metersphere.api.dto;

import io.metersphere.base.domain.ApiTest;
import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APITestResult extends ApiTest {

    private String projectName;

    private String userName;

    private Schedule schedule;
}
