package io.metersphere.project.dto.environment.assertion.body;

import lombok.Data;

import java.util.List;

@Data
public class MsRegexAssertion {
    /**
     * 断言列表
     */
    private List<MsRegexAssertionItem> assertions;
}