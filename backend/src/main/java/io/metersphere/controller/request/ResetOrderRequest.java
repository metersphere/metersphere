package io.metersphere.controller.request;

import lombok.Data;

@Data
public class ResetOrderRequest {
    private String moveId;
    private String targetId;
    private String moveMode;

    // 项目id或者测试计划id
    private String groupId;

    public enum MoveMode {
        BEFORE, AFTER
    }
}
