package io.metersphere.engine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EngineContext {
    private String engineId;
    private String engineType;
    private InputStream inputStream;
    private Map<String, Object> properties = new HashMap<>();

    public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }
}
