package io.metersphere.vo;

import lombok.Data;

import java.text.DecimalFormat;

@Data
public class ResultVO {
    private String status;
    private long scenarioSuccess;
    private int scenarioTotal;

    public void computerTotal(int total) {
        this.scenarioTotal += total;
    }

    public void computerSuccess(long success) {
        this.scenarioSuccess += success;
    }

    public String computerPassRate() {
        return new DecimalFormat("0%").format((float) this.scenarioSuccess / this.scenarioTotal);
    }
}



