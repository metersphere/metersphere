package io.metersphere.gateway.dto;

public class Module {
    private String key;
    private String status;

    public Module() {
    }

    public String getKey() {
        return this.key;
    }

    public String getStatus() {
        return this.status;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
