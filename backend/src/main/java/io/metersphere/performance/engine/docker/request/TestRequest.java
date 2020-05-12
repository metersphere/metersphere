package io.metersphere.performance.engine.docker.request;

import java.util.HashMap;
import java.util.Map;

public class TestRequest extends BaseRequest {

    private int size;
    private String fileString;
    private String image;
    private Map<String, String> testData = new HashMap<>();
    private Map<String, String> env = new HashMap<>();

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFileString() {
        return fileString;
    }

    public void setFileString(String fileString) {
        this.fileString = fileString;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, String> getTestData() {
        return testData;
    }

    public void setTestData(Map<String, String> testData) {
        this.testData = testData;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }
}
