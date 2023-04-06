package io.metersphere.novice.request;

import lombok.Data;

/**
 * @author: LAN
 * @date: 2023/3/18 17:34
 * @version: 1.0
 */
@Data
public class NoviceRequest {
    /**
     * 新手引导截止步骤
     */
    private Integer guideStep;

    /**
     * 新手显示状态
     */
    private Integer status;

    /**
     * 新手任务数据
     */
    private String dataOption;
}
