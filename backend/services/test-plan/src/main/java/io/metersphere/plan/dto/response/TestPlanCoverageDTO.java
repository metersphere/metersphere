package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanCoverageDTO {
    /**
     * 未执行
     * 已执行
     */
    private int unExecute = 0;
    private int executed = 0;

    /**
     * 通过
     * 未通过
     */
    private int passed = 0;
    private int notPassed = 0;

    /**
     * 已完成
     * 进行中
     * 未开始
     * 已归档
     */
    private int finished = 0;
    private int running = 0;
    private int prepared = 0;
    private int archived = 0;

    @Schema(description = "错误码")
    private int errorCode;

    public void archivedAutoIncrement() {
        archived++;
    }

    public void notStartedAutoIncrement() {
        prepared++;
        unExecute++;
        notPassed++;
    }

    public void successAutoIncrement() {
        executed++;
        passed++;
        finished++;
    }

    public void unSuccessAutoIncrement() {
        executed++;
        notPassed++;
        finished++;
    }

    public void testPlanRunningAutoIncrement() {
        executed++;
        notPassed++;
        running++;
    }
}
