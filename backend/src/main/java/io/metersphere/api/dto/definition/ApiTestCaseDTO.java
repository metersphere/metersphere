package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiTestCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiTestCaseDTO extends ApiTestCase {
    private String moduleId;
    private String path;
    private String protocol;
    private String casePath;
    private String updateUser;
    private String createUser;
    private String deleteUser;
    private String apiName;
}
