package io.metersphere.reportstatistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseCountTableDTO implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("allCount")
    private String allCount;
    @JsonProperty("testCaseCount")
    private String testCaseCount;
    @JsonProperty("apiCaseCount")
    private String apiCaseCount;
    @JsonProperty("scenarioCaseCount")
    private String scenarioCaseCount;
    @JsonProperty("loadCaseCount")
    private String loadCaseCount;
    @JsonProperty("children")
    private List<TestCaseCountTableDTO> children;

    public TestCaseCountTableDTO(String name, long testCaseCount, long apiCaseCount, long scenarioCaseCount, long loadCaseCount) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.testCaseCount = String.valueOf(testCaseCount);
        this.apiCaseCount = String.valueOf(apiCaseCount);
        this.scenarioCaseCount = String.valueOf(scenarioCaseCount);
        this.loadCaseCount = String.valueOf(loadCaseCount);
        this.allCount = String.valueOf(testCaseCount + apiCaseCount + scenarioCaseCount + loadCaseCount);

        children = new ArrayList<>();
    }
}
