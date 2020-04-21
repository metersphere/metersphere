package io.metersphere.report.base;

public class Statistics {

    private String label;

    private String samples;

    private String ko;

    private String error;

    private String average;

    private String min;

    private String max;

    private String tp90;

    private String tp95;

    private String tp99;

    private String transactions;

    private String received;

    private String sent;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSamples() {
        return samples;
    }

    public void setSamples(String samples) {
        this.samples = samples;
    }

    public String getKo() {
        return ko;
    }

    public void setKo(String ko) {
        this.ko = ko;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
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

    public String getTransactions() {
        return transactions;
    }

    public void setTransactions(String transactions) {
        this.transactions = transactions;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getReceived() {
        return received;
    }

    public void setReceived(String received) {
        this.received = received;
    }
}
