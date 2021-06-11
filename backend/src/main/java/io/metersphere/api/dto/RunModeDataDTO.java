package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.jorphan.collections.HashTree;

@Getter
@Setter
public class RunModeDataDTO {
    // 执行HashTree
    private HashTree hashTree;
    // 测试场景/测试用例
    private String testId;
    // 报告id
    private String reportId;
    public RunModeDataDTO(String testId,String reportId) {
        this.testId = testId;
        this.reportId = reportId;
    }

    public RunModeDataDTO(HashTree hashTree,String reportId) {
        this.hashTree = hashTree;
        this.reportId = reportId;
    }
}
