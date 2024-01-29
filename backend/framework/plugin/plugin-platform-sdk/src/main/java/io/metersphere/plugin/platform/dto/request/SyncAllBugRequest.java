package io.metersphere.plugin.platform.dto.request;

import lombok.Data;

import java.util.function.Consumer;

@Data
public class SyncAllBugRequest extends SyncBugRequest {

    /**
     * 创建时间前后
     */
    private Boolean pre;

    /**
     * 条件: 缺陷创建时间
     */
    private Long createTime;

    /**
     * 同步后置方法
     */
    private Consumer<SyncPostParamRequest> syncPostProcessFunc;
}
