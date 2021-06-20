package io.metersphere.performance.engine;

import java.util.HashMap;
import java.util.Map;

public class EngineContext {
    private String testId;
    private String testName;
    private String namespace;
    private String fileType;
    private String content;
    private String resourcePoolId;
    private String reportId;
    private Integer resourceIndex;
    private double[] ratios;
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, byte[]> testResourceFiles = new HashMap<>();

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public void addProperties(Map<String, Object> props) {
        this.properties.putAll(props);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getResourcePoolId() {
        return resourcePoolId;
    }

    public void setResourcePoolId(String resourcePoolId) {
        this.resourcePoolId = resourcePoolId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }


    public Integer getResourceIndex() {
        return resourceIndex;
    }

    public void setResourceIndex(Integer resourceIndex) {
        this.resourceIndex = resourceIndex;
    }

    public double[] getRatios() {
        return ratios;
    }

    public void setRatios(double[] ratios) {
        this.ratios = ratios;
    }

    public Map<String, byte[]> getTestResourceFiles() {
        return testResourceFiles;
    }

    public void setTestResourceFiles(Map<String, byte[]> testResourceFiles) {
        this.testResourceFiles = testResourceFiles;
    }
}
