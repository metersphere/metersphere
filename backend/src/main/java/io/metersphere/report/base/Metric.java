package io.metersphere.report.base;

import com.opencsv.bean.CsvBindByName;

public class Metric {
    //    timestamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect

    @CsvBindByName(column = "timestamp") // 访问开始时间
    private String timestamp;
    @CsvBindByName(column = "elapsed") // 访问开始到结束的用时 - 响应时间
    private String elapsed;
    @CsvBindByName(column = "label") // 请求的标签
    private String label;
    @CsvBindByName(column = "responseCode") // 响应码
    private String responseCode;
    @CsvBindByName(column = "responseMessage") // 响应信息
    private String responseMessage;
    @CsvBindByName(column = "threadName") // 请求所属线程
    private String threadName;
    @CsvBindByName(column = "dataType") // 数据类型
    private String dataType;
    @CsvBindByName(column = "success") // 访问是否成功
    private String success;
    @CsvBindByName(column = "failureMessage") // 访问失败信息
    private String failureMessage;
    @CsvBindByName(column = "bytes") //
    private String bytes;
    @CsvBindByName(column = "sentBytes") //
    private String sentBytes;
    @CsvBindByName(column = "grpThreads") // 线程组
    private String grpThreads;
    @CsvBindByName(column = "allThreads") //
    private String allThreads;
    @CsvBindByName(column = "URL") //
    private String url;
    @CsvBindByName(column = "Latency") // 延时
    private String latency;
    @CsvBindByName(column = "IdleTime") // 闲置时间
    private String idleTime;
    @CsvBindByName(column = "Connect") //
    private String connect;


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getElapsed() {
        return elapsed;
    }

    public void setElapsed(String elapsed) {
        this.elapsed = elapsed;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getBytes() {
        return bytes;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public String getSentBytes() {
        return sentBytes;
    }

    public void setSentBytes(String sentBytes) {
        this.sentBytes = sentBytes;
    }

    public String getGrpThreads() {
        return grpThreads;
    }

    public void setGrpThreads(String grpThreads) {
        this.grpThreads = grpThreads;
    }

    public String getAllThreads() {
        return allThreads;
    }

    public void setAllThreads(String allThreads) {
        this.allThreads = allThreads;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLatency() {
        return latency;
    }

    public void setLatency(String latency) {
        this.latency = latency;
    }

    public String getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(String idleTime) {
        this.idleTime = idleTime;
    }

    public String getConnect() {
        return connect;
    }

    public void setConnect(String connect) {
        this.connect = connect;
    }
}
