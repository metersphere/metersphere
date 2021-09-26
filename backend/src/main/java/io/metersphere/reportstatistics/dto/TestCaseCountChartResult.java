package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseCountChartResult {
    private String groupName;
    private long countNum;

    public String getCountNumStr(){
        return String.valueOf(countNum);
    }
}
