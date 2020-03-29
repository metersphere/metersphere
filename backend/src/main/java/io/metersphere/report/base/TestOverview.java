package io.metersphere.report.base;

public class TestOverview {

    private String maxUsers;
    private String avgThroughput; // Hits/s
    private String errors;
    private String avgResponseTime; // s
    private String responseTime90;
    private String avgBandwidth;

    public String getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(String maxUsers) {
        this.maxUsers = maxUsers;
    }

    public String getAvgThroughput() {
        return avgThroughput;
    }

    public void setAvgThroughput(String avgThroughput) {
        this.avgThroughput = avgThroughput;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(String avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public String getResponseTime90() {
        return responseTime90;
    }

    public void setResponseTime90(String responseTime90) {
        this.responseTime90 = responseTime90;
    }

    public String getAvgBandwidth() {
        return avgBandwidth;
    }

    public void setAvgBandwidth(String avgBandwidth) {
        this.avgBandwidth = avgBandwidth;
    }
}
