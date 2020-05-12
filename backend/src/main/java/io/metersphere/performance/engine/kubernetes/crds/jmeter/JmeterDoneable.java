package io.metersphere.performance.engine.kubernetes.crds.jmeter;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class JmeterDoneable extends CustomResourceDoneable<Jmeter> {
    public JmeterDoneable(Jmeter resource, Function<Jmeter, Jmeter> function) {
        super(resource, function);
    }
}
