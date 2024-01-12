package io.metersphere.project.dto.environment.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("ENV_RESPONSE_TIME")
public class MsResponseTimeAssertion extends MsAssertion {
    /**
     * 最大响应时间
     * 响应时间在xx毫秒内
     */
    private Long expectedValue;
}
