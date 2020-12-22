package io.metersphere.performance.base;

import lombok.Data;

@Data
public class TestOverview {

    private String maxUsers;
    private String avgThroughput;
    private String errors;
    private String avgResponseTime;
    private String responseTime90;
    private String avgBandwidth;
    private String avgTransactions;
}
