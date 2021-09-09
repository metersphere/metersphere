package io.metersphere.controller.request;

import lombok.Data;

@Data
public class ResetOrderRequest {
    private String moveId;
    private String targetId;
    private String moveMode;
    private String projectId;

    public enum MoveMode {
        BEFORE, AFTER
    }
}
