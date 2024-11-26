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
        this.archived++;
    }

    public void notStartedAutoIncrement(boolean isArchived) {
        this.unExecute++;
        this.notPassed++;
        if (isArchived) {
            this.archived++;
        } else {
            this.prepared++;
        }
    }

    public void passAndFinishedAutoIncrement(boolean isArchived) {
        this.executed++;
        this.passed++;

        if (isArchived) {
            this.archived++;
        } else {
            this.finished++;
        }
    }

    public void unSuccessAutoIncrement(boolean isArchived) {
        this.executed++;
        this.notPassed++;
        if (isArchived) {
            this.archived++;
        } else {
            this.finished++;
        }
    }

    public void passAndNotFinishedAutoIncrement(boolean isArchived) {
        this.executed++;
        this.passed++;
        if (isArchived) {
            this.archived++;
        } else {
            this.running++;
        }
    }

    public void testPlanRunningAutoIncrement(boolean isArchived) {
        this.executed++;
        this.notPassed++;
        if (isArchived) {
            this.archived++;
        } else {
            this.running++;
        }
    }
}
