package io.metersphere.api.engine.provider;

import io.fabric8.kubernetes.client.KubernetesClient;

public interface KubernetesApiProvider {
    KubernetesClient getClient();
}
