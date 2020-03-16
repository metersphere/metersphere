package io.metersphere.engine.kubernetes.crds;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class MeterSphereCustomResourceDoneable<T extends MeterSphereCustomResource> extends CustomResourceDoneable<T> {
    public MeterSphereCustomResourceDoneable(T resource, Function<T, T> function) {
        super(resource, function);
    }
}
