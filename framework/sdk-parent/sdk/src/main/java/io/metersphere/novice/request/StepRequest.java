package io.metersphere.novice.request;

import lombok.Data;

/**
 * @author: LAN
 * @date: 2023/3/18 17:34
 * @version: 1.0
 */
@Data
public class StepRequest {
    /**
     * 新手引导截止步骤
     */
    private Integer guideStep;
}
