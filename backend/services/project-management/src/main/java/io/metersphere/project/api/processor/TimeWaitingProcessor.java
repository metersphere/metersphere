package io.metersphere.project.api.processor;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * 等待时间处理器
 * @Author: jianxing
 * @CreateTime: 2023-11-07  09:59
 */
@Data
@JsonTypeName("TIME_WAITING")
public class TimeWaitingProcessor extends MsProcessor {
    /**
     * 等待时间
     * 单位：毫秒
     */
    private Long delay;
}
