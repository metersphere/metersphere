package io.metersphere.api.dto;

import io.metersphere.base.domain.ApiTestWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APITestResult extends ApiTestWithBLOBs {

    private String projectName;
}
