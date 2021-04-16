package io.metersphere.api.dto.mockconfig;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/4/13 4:00 下午
 * @Description
 */
@Getter
@Setter
public class MockExpectConfigRequest {

    private String id;

    private String mockConfigId;

    private String name;

    private List<String> tags;

    private Object request;

    private Object response;

    private String status;
}
