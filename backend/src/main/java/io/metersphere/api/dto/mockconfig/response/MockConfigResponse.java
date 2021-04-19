package io.metersphere.api.dto.mockconfig.response;

import io.metersphere.base.domain.MockConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/4/13 4:59 下午
 * @Description
 */
@Getter
@Setter
@AllArgsConstructor
public class MockConfigResponse {
    private MockConfig mockConfig;
    private List<MockExpectConfigResponse> mockExpectConfigList;
}
