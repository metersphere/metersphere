package io.metersphere.performance.engine.kubernetes.crds;

import io.fabric8.kubernetes.client.CustomResource;

public class MeterSphereCustomResource extends CustomResource {

    private String crd;

    private Object spec;

    private Object status;

    public String getCrd() {
        return crd;
    }

    public void setCrd(String crd) {
        this.crd = crd;
    }

    public Object getSpec() {
        return spec;
    }

    public void setSpec(Object spec) {
        this.spec = spec;
    }

    public Object getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status;
    }
}
