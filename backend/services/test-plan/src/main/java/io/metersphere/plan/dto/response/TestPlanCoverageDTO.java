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
     * 归档通过
     * 未通过
     * 归档未通过
     */
    private int passed = 0;
    private int passedArchived = 0;
    private int notPassed = 0;
    private int notPassedArchived = 0;

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
        this.archived++;
    }

    public void notStartedAutoIncrement(boolean isArchived) {
        this.unExecute++;

        if (isArchived) {
            this.archived++;
            this.notPassedArchived++;
        } else {
            this.prepared++;
            this.notPassed++;
        }
    }

    public void passAndFinishedAutoIncrement(boolean isArchived) {
        this.executed++;

        if (isArchived) {
            this.archived++;
            this.passedArchived++;
        } else {
            this.finished++;
            this.passed++;
        }
    }

    public void unSuccessAutoIncrement(boolean isArchived) {
        this.executed++;

        if (isArchived) {
            this.archived++;
            this.notPassedArchived++;
        } else {
            this.finished++;
            this.notPassed++;
        }
    }

    public void passAndNotFinishedAutoIncrement(boolean isArchived) {
        this.executed++;

        if (isArchived) {
            this.archived++;
            this.passedArchived++;
        } else {
            this.running++;
            this.passed++;
        }
    }

    public void testPlanRunningAutoIncrement(boolean isArchived) {
        this.executed++;

        if (isArchived) {
            this.archived++;
            this.notPassedArchived++;
        } else {
            this.running++;
            this.notPassed++;
        }
    }
}
