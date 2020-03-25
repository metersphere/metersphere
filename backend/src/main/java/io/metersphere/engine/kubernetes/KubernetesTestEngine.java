package io.metersphere.engine.kubernetes;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.engine.AbstractEngine;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.kubernetes.crds.jmeter.Jmeter;
import io.metersphere.engine.kubernetes.crds.jmeter.JmeterSpec;
import io.metersphere.engine.kubernetes.provider.ClientCredential;
import io.metersphere.engine.kubernetes.provider.KubernetesProvider;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;

public class KubernetesTestEngine extends AbstractEngine {

    @Override
    public boolean init(LoadTestWithBLOBs loadTest) {
        super.init(loadTest);
        return true;
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
                MSException.throwException("资源不足");
            }
            try {
                EngineContext context = EngineFactory.createContext(loadTest, jmxFile, csvFiles, threadNum);
                runTest(context, clientCredential, 1);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
    }

    private void runTest(EngineContext context, ClientCredential credential, int replicas) {
        KubernetesProvider kubernetesProvider = new KubernetesProvider(JSON.toJSONString(credential));
        // create namespace
        kubernetesProvider.confirmNamespace(context.getNamespace());
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
                setReplicas(replicas);
                setImage("registry.fit2cloud.com/metersphere/jmeter-master:0.0.2");
            }});
            LogUtil.info("Load test started. " + context.getTestId());
            kubernetesProvider.applyCustomResource(jmeter);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    @Override
    public void stop() {

    }
}
