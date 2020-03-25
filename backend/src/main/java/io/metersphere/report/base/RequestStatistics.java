package io.metersphere.report.base;

public class RequestStatistics {

    /**请求标签*/
    private String requestLabel;

    /**压测请求数*/
    private Integer samples;

    /**平均响应时间*/
    private String average;

    /**平均点击率*/
    private Double avgHits;

    /**90% Line*/
    private String tp90;

    /**95% Line*/
    private String tp95;

    /**99% Line*/
    private String tp99;

    /**最小请求时间 Min Response Time /ms */
    private String min;

    /**最大请求时间 Max Response Time /ms */
    private String max;

    /**吞吐量 KB/sec*/
    private String kbPerSec;

    /**错误率 Error Percentage */
    private String errors;

    public String getRequestLabel() {
        return requestLabel;
    }

    public void setRequestLabel(String requestLabel) {
        this.requestLabel = requestLabel;
    }

    public Integer getSamples() {
        return samples;
    }

    public void setSamples(Integer samples) {
        this.samples = samples;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    public Double getAvgHits() {
        return avgHits;
    }

    public void setAvgHits(Double avgHits) {
        this.avgHits = avgHits;
    }

    public String getTp90() {
        return tp90;
    }

    public void setTp90(String tp90) {
        this.tp90 = tp90;
    }

    public String getTp95() {
        return tp95;
    }

    public void setTp95(String tp95) {
        this.tp95 = tp95;
    }

    public String getTp99() {
        return tp99;
    }

    public void setTp99(String tp99) {
        this.tp99 = tp99;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getKbPerSec() {
        return kbPerSec;
    }

    public void setKbPerSec(String kbPerSec) {
        this.kbPerSec = kbPerSec;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }
}
