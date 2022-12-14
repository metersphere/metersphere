package io.metersphere.task.dto;

import lombok.Data;

@Data
public class TaskStatisticsDTO {
    private int apiTotal;
    private int scenarioTotal;
    private int perfTotal;
    // 总量
    private int total;

    public int getTotal() {
        return apiTotal + scenarioTotal + perfTotal;
    }
}
