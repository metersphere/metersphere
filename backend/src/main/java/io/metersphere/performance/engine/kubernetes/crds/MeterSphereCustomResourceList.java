package io.metersphere.performance.engine.kubernetes.crds;

import io.fabric8.kubernetes.client.CustomResourceList;

public class MeterSphereCustomResourceList<T extends MeterSphereCustomResource> extends CustomResourceList<T> {
}
