package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiCountChartResult {
    private String groupName;
    private long countNum;

    public String getCountNumStr() {
        return String.valueOf(countNum);
    }
}
