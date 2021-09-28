package io.metersphere.api.dto.mockconfig;

import io.metersphere.base.domain.MockExpectConfigWithBLOBs;
import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/9/27 5:54 下午
 */
@Getter
@Setter
public class MockConfigImportDTO extends MockExpectConfigWithBLOBs {
    public String apiId;
}
