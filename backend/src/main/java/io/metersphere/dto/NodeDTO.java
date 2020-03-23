package io.metersphere.dto;

public class NodeDTO {
    private String ip;
    private Integer port;
    private Integer maxConcurrency;
    private Boolean validate;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public Boolean getValidate() {
        return validate;
    }

    public void setValidate(Boolean validate) {
        this.validate = validate;
    }
}
