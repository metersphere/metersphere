package io.metersphere.xpack.resourcepool.engine.provider;

import io.metersphere.engine.request.StartTestRequest;

public interface KubernetesProvider {
    void deployJmeter(StartTestRequest request, ClientCredential clientCredential);

    void deleteJmeter(String testId, String namespace);
}
