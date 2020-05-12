package io.metersphere.performance.engine.kubernetes.crds.jmeter;

import io.metersphere.performance.engine.kubernetes.crds.MeterSphereCustomResource;

public class Jmeter extends MeterSphereCustomResource {
    public static final String CRD = "jmeters.metersphere.io";
    public static final String KIND = "Jmeter";
    private JmeterSpec spec;
    private JmeterStatus status;

    public Jmeter() {
        this.setCrd(CRD);
        this.setKind(KIND);
    }

    @Override
    public JmeterSpec getSpec() {
        return spec;
    }

    public void setSpec(JmeterSpec spec) {
        this.spec = spec;
    }

    @Override
    public JmeterStatus getStatus() {
        return status;
    }

    public void setStatus(JmeterStatus status) {
        this.status = status;
    }
}
