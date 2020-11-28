package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiTestCase;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiTestCaseResult extends ApiTestCase {
    private String projectName;
    private String createUser;
    private String updateUser;
    private String execResult;
    private boolean active = false;
}
