package io.metersphere.sdk.dto.api.request.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  09:59
 */
@Data
@JsonTypeName("TIME_WAITING")
public class TimeWaitingProcessor extends MsProcessor {
    private Integer delay;
}
