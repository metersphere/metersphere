package io.metersphere.sdk.dto.api.result;

import lombok.Data;

@Data
public class ProcessResultDTO {
    // 报告状态
    private String status;

    // TODO 总数,放报告生成后计算
    private long total;

    // 成功总数
    private long successCount;
    // 误报总数
    private long fakeErrorCount;
    // 失败总数
    private long errorCount;

    // TODO 根据初始步骤大小，在执行过程中进行标记； 初始大小 - 过程标记步骤
    private long pendingCount;

    // 断言总数
    private long assertionCount;

    // 成功断言数总数
    private long successAssertionCount;

    // 请求总时长
    private long requestDuration;

    // 标记执行步骤中是否有脚本报错，记录最后一次错误信息
    private String lastScriptIdentifier;

    public void computerTotal() {
        this.total = this.pendingCount + this.errorCount + this.successCount + this.fakeErrorCount;
    }

    public void computerFakeError(long fakeErrorCount) {
        this.fakeErrorCount += fakeErrorCount;
    }

    public void computerSuccess(long success) {
        this.successCount += success;
    }

    public void computerError(long error) {
        this.errorCount += error;
    }

    public void computerAssertion(long assertion) {
        this.assertionCount += assertion;
    }

    public void computerSuccessAssertion(long successAssertion) {
        this.successAssertionCount += successAssertion;
    }

    public void computerRequestDuration(long requestDuration) {
        this.requestDuration += requestDuration;
    }

}



