package io.metersphere.performance.engine.producer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Metric {
    @JsonProperty("test.id")
    private String testId;
    @JsonProperty("test.name")
    private String testName;
    @JsonProperty("test.startTime")
    private Long clusterStartTime;
    @JsonProperty("test.reportId")
    private String reportId;
    @JsonProperty("ContentType")
    private String contentType;
    @JsonProperty("IdleTime")
    private Integer idleTime;
    @JsonProperty("ElapsedTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private Date elapsedTime;
    @JsonProperty("ErrorCount")
    private Integer errorCount;
    @JsonProperty("Timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private Date timestamp;
    @JsonProperty("URL")
    private String url;
    @JsonProperty("SampleStartTime")
    private String sampleStartTime;
    @JsonProperty("Success")
    private Boolean success;
    @JsonProperty("Bytes")
    private Integer bytes;
    @JsonProperty("SentBytes")
    private Integer sentBytes;
    @JsonProperty("AllThreads")
    private Integer allThreads;
    @JsonProperty("TestElement.name")
    private String testElementName;
    @JsonProperty("DataType")
    private String dataType;
    @JsonProperty("ResponseTime")
    private Integer responseTime;
    @JsonProperty("SampleCount")
    private Integer sampleCount;
    @JsonProperty("FailureMessage")
    private String failureMessage;
    @JsonProperty("ConnectTime")
    private Integer connectTime;
    @JsonProperty("ResponseCode")
    private String responseCode;
    @JsonProperty("TestStartTime")
    private Long testStartTime;
    @JsonProperty("AssertionResults")
    private List<Object> assertionResults;
    @JsonProperty("Latency")
    private Integer latency;
    @JsonProperty("InjectorHostname")
    private String injectorHostname;
    @JsonProperty("GrpThreads")
    private Integer grpThreads;
    @JsonProperty("SampleEndTime")
    private String sampleEndTime;
    @JsonProperty("BodySize")
    private Long bodySize;
    @JsonProperty("ThreadName")
    private String threadName;
    @JsonProperty("SampleLabel")
    private String sampleLabel;

}
