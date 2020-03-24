package io.metersphere.engine.kubernetes;

import com.alibaba.fastjson.JSON;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.kubernetes.crds.jmeter.Jmeter;
import io.metersphere.engine.kubernetes.crds.jmeter.JmeterSpec;
import io.metersphere.engine.kubernetes.provider.ClientCredential;
import io.metersphere.engine.kubernetes.provider.KubernetesProvider;
import io.metersphere.service.TestResourcePoolService;
import io.metersphere.service.TestResourceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;

public class KubernetesTestEngine implements Engine {
    private EngineContext context;
    private TestResourcePoolService testResourcePoolService;
    private TestResourceService testResourceService;

    @Override
    public boolean init(EngineContext context) {
        this.context = context;
        this.testResourcePoolService = CommonBeanFactory.getBean(TestResourcePoolService.class);
        this.testResourceService = CommonBeanFactory.getBean(TestResourceService.class);
        return true;
    }


    @Override
    public void start() {
        if (context == null) {
            LogUtil.warn("Please initial the engine.");
            return;
        }
        TestResourcePool resourcePool = testResourcePoolService.getResourcePool(context.getResourcePoolId());
        if (resourcePool == null) {
            MSException.throwException("Resource Pool is empty");
        }
        if (!ResourcePoolTypeEnum.K8S.name().equals(resourcePool.getType())) {
            MSException.throwException("Invalid Resource Pool type.");
        }
        List<TestResource> resourceList = testResourceService.getResourcesByPoolId(resourcePool.getId());
        if (CollectionUtils.isEmpty(resourceList)) {
            MSException.throwException("Test Resource is empty");
        }
        // resourceList size 1
        resourceList.forEach(r -> {
            String configuration = r.getConfiguration();
            ClientCredential clientCredential = JSON.parseObject(configuration, ClientCredential.class);
            runTest(clientCredential);
        });
    }

    private void runTest(ClientCredential credential) {
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
