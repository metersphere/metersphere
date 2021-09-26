package io.metersphere.reportstatistics.dto;

/**
 * @author song.tianyang
 * @Date 2021/9/8 5:36 下午
 */
public class TestCaseCountSummary {
    public String groupName;

    public long testCaseCount = 0;
    public long apiCaseCount = 0;
    public long scenarioCaseCount = 0;
    public long loadCaseCount = 0;

    public TestCaseCountSummary(String groupName) {
        this.groupName = groupName;
    }

    public long getAllCount() {
        return this.testCaseCount + this.apiCaseCount + this.scenarioCaseCount + this.loadCaseCount;
    }
}
