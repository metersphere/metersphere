package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TestAnalysisTableDTO {
    private String id;
    private String name;
    private String createCount;
    private String updateCount;
    private List<TestAnalysisTableDTO> children;

    public TestAnalysisTableDTO() {

    }

    public TestAnalysisTableDTO(String name, String createCount, String updateCount, List<TestAnalysisTableDTO> children) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createCount = createCount;
        this.updateCount = updateCount;
        this.children = children;
    }
}
