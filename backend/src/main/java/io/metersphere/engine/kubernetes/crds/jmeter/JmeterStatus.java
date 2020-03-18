package io.metersphere.engine.kubernetes.crds.jmeter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JmeterStatus implements KubernetesResource {
    private String phase;
    private String reason;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "JmeterStatus{" +
                "phase='" + phase + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
