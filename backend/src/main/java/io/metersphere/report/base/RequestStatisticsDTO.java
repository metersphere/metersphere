package io.metersphere.report.base;

import java.util.List;

public class RequestStatisticsDTO extends RequestStatistics {

    private List<RequestStatistics> requestStatisticsList;

    private String totalLabel;

    private String totalSamples;

    private String totalErrors;

    private String totalAverage;

    private String totalMin;

    private String totalMax;

    private String totalTP90;

    private String totalTP95;

    private String totalTP99;

    private String totalAvgBandwidth;

    public List<RequestStatistics> getRequestStatisticsList() {
        return requestStatisticsList;
    }

    public void setRequestStatisticsList(List<RequestStatistics> requestStatisticsList) {
        this.requestStatisticsList = requestStatisticsList;
    }

    public String getTotalLabel() {
        return totalLabel;
    }

    public void setTotalLabel(String totalLabel) {
        this.totalLabel = totalLabel;
    }

    public String getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(String totalSamples) {
        this.totalSamples = totalSamples;
    }

    public String getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(String totalErrors) {
        this.totalErrors = totalErrors;
    }

    public String getTotalAverage() {
        return totalAverage;
    }

    public void setTotalAverage(String totalAverage) {
        this.totalAverage = totalAverage;
    }

    public String getTotalMin() {
        return totalMin;
    }

    public void setTotalMin(String totalMin) {
        this.totalMin = totalMin;
    }

    public String getTotalMax() {
        return totalMax;
    }

    public void setTotalMax(String totalMax) {
        this.totalMax = totalMax;
    }

    public String getTotalTP90() {
        return totalTP90;
    }

    public void setTotalTP90(String totalTP90) {
        this.totalTP90 = totalTP90;
    }

    public String getTotalTP95() {
        return totalTP95;
    }

    public void setTotalTP95(String totalTP95) {
        this.totalTP95 = totalTP95;
    }

    public String getTotalTP99() {
        return totalTP99;
    }

    public void setTotalTP99(String totalTP99) {
        this.totalTP99 = totalTP99;
    }

    public String getTotalAvgBandwidth() {
        return totalAvgBandwidth;
    }

    public void setTotalAvgBandwidth(String totalAvgBandwidth) {
        this.totalAvgBandwidth = totalAvgBandwidth;
    }
}
