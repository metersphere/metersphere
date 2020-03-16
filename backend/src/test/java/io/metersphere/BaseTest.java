package io.metersphere;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;

public class BaseTest {
    protected KubernetesClient kubernetesClient;

    @Before
    public void before() {

        try {
            ConfigBuilder configBuilder = new ConfigBuilder();
            configBuilder.withMasterUrl("https://172.16.10.93:6443");
            kubernetesClient = new DefaultKubernetesClient(configBuilder.build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void test1() {
        PodList list = kubernetesClient.pods().list();
        for (Pod item : list.getItems()) {
            System.out.println(item);
        }
    }
}
