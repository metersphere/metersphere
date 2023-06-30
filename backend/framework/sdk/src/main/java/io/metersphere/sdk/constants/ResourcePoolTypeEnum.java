package io.metersphere.sdk.constants;

public enum ResourcePoolTypeEnum {
    /**
     * node controller 资源池
     */
    NODE("Node"),
    K8S("Kubernetes");

    private  String name;

    ResourcePoolTypeEnum(String name) {
       this.name = name;
    }

}