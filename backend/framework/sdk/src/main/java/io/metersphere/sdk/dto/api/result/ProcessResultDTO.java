package io.metersphere.sdk.dto.api.result;

import lombok.Data;

@Data
public class ProcessResultDTO {
    private String status;
    private long scenarioSuccess;
    private int scenarioTotal;

    public void computerTotal(int total) {
        this.scenarioTotal += total;
    }

    public void computerSuccess(long success) {
        this.scenarioSuccess += success;
    }

    public long computerPassRate() {
        if (this.scenarioTotal == 0) return 0;
        return this.scenarioSuccess / this.scenarioTotal;
    }
}



