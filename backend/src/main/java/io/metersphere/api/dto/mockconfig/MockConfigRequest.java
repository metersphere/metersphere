package io.metersphere.api.dto.mockconfig;

import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2021/4/13 4:00 下午
 * @Description
 */
@Getter
@Setter
public class MockConfigRequest {
    private String id;
    private String apiId;
    private String projectId;
}
