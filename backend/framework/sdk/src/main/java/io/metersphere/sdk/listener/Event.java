package io.metersphere.sdk.listener;

public class Event {
    private String message;
    private String module;

    public Event(String module, String message) {
        this.module = module;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getModule() {
        return module;
    }
}
