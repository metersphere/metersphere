package io.metersphere.project.api.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * 响应时间断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("RESPONSE_TIME")
public class MsResponseTimeAssertion extends MsAssertion {
    /**
     * 最大响应时间
     * 响应时间在xx毫秒内
     */
   private Long expectedValue;
}
