package io.metersphere.sdk.constants;

import lombok.Getter;

@Getter
public enum ResourcePoolTypeEnum {
    /**
     * node controller 资源池
     */
    NODE("Node"),
    K8S("Kubernetes");

    private final String name;

    ResourcePoolTypeEnum(String name) {
        this.name = name;
    }

}