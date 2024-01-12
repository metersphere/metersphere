package io.metersphere.project.dto.environment.assertion;

import lombok.Data;

import java.util.List;

@Data
public class MsAssertionConfig {
    /**
     * 断言列表
     */
    private List<MsAssertion> assertions;
}
