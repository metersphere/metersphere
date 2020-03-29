package io.metersphere.engine.kubernetes;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.engine.AbstractEngine;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.kubernetes.crds.jmeter.Jmeter;
import io.metersphere.engine.kubernetes.crds.jmeter.JmeterSpec;
import io.metersphere.engine.kubernetes.provider.ClientCredential;
import io.metersphere.engine.kubernetes.provider.KubernetesProvider;
import io.metersphere.engine.kubernetes.registry.RegistryService;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;

public class KubernetesTestEngine extends AbstractEngine {

    private RegistryService registryService;

    public KubernetesTestEngine(LoadTestWithBLOBs loadTest) {
        this.init(loadTest);
    }

    @Override
    public void init(LoadTestWithBLOBs loadTest) {
        super.init(loadTest);
        this.registryService = CommonBeanFactory.getBean(RegistryService.class);
    }


    @Override
    public void start() {
        Integer sumThreadNum = getRunningThreadNum();
        // resourceList size 1
        resourceList.forEach(r -> {
            String configuration = r.getConfiguration();
            ClientCredential clientCredential = JSON.parseObject(configuration, ClientCredential.class);
            // 最大并发数
            Integer maxConcurrency = clientCredential.getMaxConcurrency();
            // 当前测试需要的并发数大于剩余的并发数报错
            if (threadNum > maxConcurrency - sumThreadNum) {
                MSException.throwException("Insufficient resources");
            }
            try {
                EngineContext context = EngineFactory.createContext(loadTest, threadNum);
                runTest(context, clientCredential);
            } catch (Exception e) {
                MSException.throwException(e);
            }
        });
    }

    private void runTest(EngineContext context, ClientCredential credential) {
        KubernetesProvider kubernetesProvider = new KubernetesProvider(JSON.toJSONString(credential));

        // create namespace
        kubernetesProvider.confirmNamespace(context.getNamespace());
        // docker registry
        registryService.dockerRegistry(kubernetesProvider, context.getNamespace());
        // create cm
        try (KubernetesClient client = kubernetesProvider.getKubernetesClient()) {
            String configMapName = context.getTestId() + "-files";
            ConfigMap configMap = client.configMaps().inNamespace(context.getNamespace()).withName(configMapName).get();
            if (configMap == null) {
                ConfigMap item = new ConfigMap();
                item.setMetadata(new ObjectMeta() {{
                    setName(configMapName);
                }});
                item.setData(new HashMap<String, String>() {{
                    put(context.getTestId() + ".jmx", context.getContent());
                    if (MapUtils.isNotEmpty(context.getTestData())) {
                        putAll(context.getTestData());
                    }
                }});
                client.configMaps().inNamespace(context.getNamespace()).create(item);
            }
        }
        // create jmeter
        // todo image
        try {
            Jmeter jmeter = new Jmeter();
            jmeter.setMetadata(new ObjectMeta() {{
                setNamespace(context.getNamespace());
                setName(context.getTestId());
            }});
            jmeter.setSpec(new JmeterSpec() {{
                setReplicas(1);
                setImage(registryService.getRegistry() + JMETER_IMAGE);
            }});
            LogUtil.info("Load test started. " + context.getTestId());
            kubernetesProvider.applyCustomResource(jmeter);
        } catch (Exception e) {
            MSException.throwException(e);
        }
    }

    @Override
    public void stop() {
        resourceList.forEach(r -> {
            try {
                String configuration = r.getConfiguration();
                ClientCredential clientCredential = JSON.parseObject(configuration, ClientCredential.class);
                KubernetesProvider provider = new KubernetesProvider(JSON.toJSONString(clientCredential));
                provider.confirmNamespace(loadTest.getProjectId());
                Jmeter jmeter = new Jmeter();
                jmeter.setMetadata(new ObjectMeta() {{
                    setName(loadTest.getId());
                    setNamespace(loadTest.getProjectId());
                }});
                jmeter.setSpec(new JmeterSpec() {{
                    setReplicas(1);
                    setImage(registryService.getRegistry() + JMETER_IMAGE);
                }});
                provider.deleteCustomResource(jmeter);
            } catch (Exception e) {
                MSException.throwException(e);
            }

        });

    }
}
