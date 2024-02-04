package io.metersphere.project.dto.environment;

import io.metersphere.project.api.assertion.MsAssertion;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 断言设置
 * @Author: jianxing
 * @CreateTime: 2023-11-23  17:26
 */
@Data
public class MsEnvAssertionConfig  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 断言列表
     */
    private List<MsAssertion> assertions = new ArrayList<>(0);
}
