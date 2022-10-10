package io.metersphere.api.dto.mock;

import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MockExpectConfigDTO{
    private String projectId;
    private MockExpectConfigWithBLOBs mockExpectConfig;
}
