package io.metersphere.performance.engine.kubernetes.crds.jmeter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.HashMap;
import java.util.Map;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JmeterSpec implements KubernetesResource {
    private int replicas = 1;
    private String image;
    private Map<String, String> env = new HashMap<>();

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }
}
