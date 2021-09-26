package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TestCaseCountTableDTO {
    private String id;
    private String name;
    private String allCount;
    private String testCaseCount;
    private String apiCaseCount;
    private String scenarioCaseCount;
    private String loadCaseCount;

    private List<TestCaseCountTableDTO> children;

    public TestCaseCountTableDTO(String name, long testCaseCount, long apiCaseCount, long scenarioCaseCount, long loadCaseCount) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.testCaseCount = String.valueOf(testCaseCount);
        this.apiCaseCount = String.valueOf(apiCaseCount);
        this.scenarioCaseCount = String.valueOf(scenarioCaseCount);
        this.loadCaseCount = String.valueOf(loadCaseCount);
        this.allCount = String.valueOf(testCaseCount+apiCaseCount+scenarioCaseCount+loadCaseCount);

        children = new ArrayList<>();
    }
}
