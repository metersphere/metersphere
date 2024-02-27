package io.metersphere.api.dto.request.controller.loop;

import lombok.Data;

@Data
public class MsCountController {
    /**
     * 循环次数
     */
    private String loops;
    /**
     * 循环间隔
     */
    private long loopTime = 0;
}
