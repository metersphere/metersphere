package io.metersphere.project.dto.environment.assertion.body;

import lombok.Data;

import java.util.List;

@Data
public class MsJSONPathAssertion {

    /**
     * 断言列表
     */
    private List<MsJSONPathAssertionItem> assertions;
}

