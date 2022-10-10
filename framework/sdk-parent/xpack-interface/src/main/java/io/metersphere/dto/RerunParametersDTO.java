package io.metersphere.dto;

import lombok.Data;

import java.util.List;

@Data
public class RerunParametersDTO {
    // 重跑报告类型/测试计划报告/集合报告
    private String type;
    // 失败重跑的报告ID
    private String reportId;

    // 失败重跑的资源
    private List<KeyValueDTO> scenarios;
    private List<KeyValueDTO> cases;
    private List<KeyValueDTO> performanceCases;
}
