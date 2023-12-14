package io.metersphere.sdk.listener;

import lombok.Data;

import java.util.Map;

@Data
public class Event {
    private String module;
    private String message;
    private Map<String, Object> paramMap;

    public Event(String module, String message) {
        this.module = module;
        this.message = message;
    }

    public Event(String module, String message, Map<String, Object> paramMap) {
        this.module = module;
        this.message = message;
        this.paramMap = paramMap;
    }


    public String module() {
        return this.module;
    }

    public String message() {
        return this.message;
    }

    public Map<String, Object> paramMap() {
        return this.paramMap;
    }
}
