package io.metersphere.api.jmeter.algorithm;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
public class ServerConfig implements Serializable {
    //服务名称
    public String url;

    //初始权重
    public int weight;

    //当前权重
    public int currentWeight;

    // 资源池并发数
    private int corePoolSize;

    // 是否开启JAR同步
    private boolean enable;

    public ServerConfig() {
    }

    public ServerConfig(String url, int weight) {
        this.url = url;
        this.weight = weight;
    }

    public ServerConfig(String url, int weight, int currentWeight) {
        this.url = url;
        this.weight = weight;
        this.currentWeight = currentWeight;
    }

    public ServerConfig(String url, int weight, int currentWeight, int corePoolSize, boolean enable) {
        this.url = url;
        this.weight = weight;
        this.currentWeight = currentWeight;
        this.corePoolSize = corePoolSize;
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "ServerConfig{" + "url='" + url + '}';
    }

    @Override
    public boolean equals(Object value) {
        if (value != null && value instanceof ServerConfig) {
            return StringUtils.equals(((ServerConfig) value).getUrl(), this.getUrl());
        }
        return false;
    }

    @Override
    public ServerConfig clone() throws CloneNotSupportedException {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCurrentWeight(this.currentWeight);
        serverConfig.setUrl(this.url);
        serverConfig.setWeight(this.weight);
        return serverConfig;
    }
}
