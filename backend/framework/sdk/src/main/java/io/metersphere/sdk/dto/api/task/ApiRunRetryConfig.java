package io.metersphere.sdk.dto.api.task;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ApiRunRetryConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 失败重试次数
     */
    private Integer retryTimes = 0;

    /**
     * 失败重试间隔(单位: ms)
     */
    private Integer retryInterval = 0;
}
