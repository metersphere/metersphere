package io.metersphere.runner;

import java.io.InputStream;

public class EngineContext {
    private String engineId;
    private InputStream inputStream;

    public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
