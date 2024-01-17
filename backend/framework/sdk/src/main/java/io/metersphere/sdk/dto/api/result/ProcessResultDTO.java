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
    // 请求执行率
    private String requestExecutionRate;
    // 请求通过率
    private String requestPassRate;
    // 断言通过率
    private String assertionPassRate;

    public void computerTotal() {
        this.total = this.pendingCount + this.errorCount + this.successCount;
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

    /**
     * 整体执行完成后调用计算执行率
     * (成功+失败 ) /总量* 100
     */
    public void computerRequestExecutionRate() {
        long count = this.successCount + this.errorCount;
        if (this.total > 0 && count > 0) {
            double executionRate = (double) count / this.total * 100;
            this.requestExecutionRate = String.format("%.2f", executionRate);
        }
    }

    /**
     * 整体完成后调用计算通过率
     * 成功总数 / 总量 * 100
     */
    public void computerRequestPassRate() {
        if (this.total > 0 && this.successCount > 0) {
            this.requestPassRate = String.format("%.2f", (double) this.successCount / this.total * 100);
        }
    }

    /**
     * 整体完成后调用计算断言通过率
     */
    public void computerAssertionPassRate() {
        if (this.assertionCount > 0 && successAssertionCount > 0) {
            this.assertionPassRate = String.format("%.2f", (double) successAssertionCount / this.assertionCount * 100);
        }
    }

}



