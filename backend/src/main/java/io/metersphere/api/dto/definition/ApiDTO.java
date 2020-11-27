package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiDTO extends TestCaseWithBLOBs {

    private String maintainerName;
    private String apiName;
    private String performName;
}
