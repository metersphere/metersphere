package io.metersphere.api.dto.mock;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MockTestDataRequest {
    //ID 返回前台，用于前台拿来取值
    private String id;
    private String condition;
    private String value;
}
