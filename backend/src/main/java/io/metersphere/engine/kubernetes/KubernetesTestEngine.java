package io.metersphere.engine.kubernetes;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.kubernetes.crds.jmeter.Jmeter;
import io.metersphere.engine.kubernetes.crds.jmeter.JmeterSpec;
import io.metersphere.engine.kubernetes.provider.ClientCredential;
import io.metersphere.engine.kubernetes.provider.KubernetesProvider;

import java.util.HashMap;

public class KubernetesTestEngine implements Engine {
    private EngineContext context;

    @Override
    public boolean init(EngineContext context) {
        // todo 初始化操作
        this.context = context;
        return true;
    }


    @Override
    public void start() {
        // todo 运行测试
        ClientCredential credential = new ClientCredential();
        credential.setMasterUrl("https://172.16.10.93:6443");
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
                    put("sample.jmx", context.getContent());
                }});
                client.configMaps().inNamespace(context.getNamespace()).create(item);
            }
        }
        // create jmeter
        try {
            Jmeter jmeter = new Jmeter();
            jmeter.setMetadata(new ObjectMeta() {{
                setNamespace(context.getNamespace());
                setName(context.getTestId());
            }});
            jmeter.setSpec(new JmeterSpec() {{
                setReplicas(1);
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
