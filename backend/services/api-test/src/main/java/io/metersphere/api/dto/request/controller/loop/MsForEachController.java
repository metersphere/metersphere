package io.metersphere.api.dto.request.controller.loop;

import lombok.Data;

@Data
public class MsForEachController {
    /**
     * 变量值 255
     */
    private String variable;
    /**
     * 变量前缀 255
     */
    private String value;
    /**
     * 循环间隔
     */
    private long loopTime = 0;

}
