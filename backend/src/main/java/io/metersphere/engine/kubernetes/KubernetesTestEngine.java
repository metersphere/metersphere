package io.metersphere.engine.kubernetes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.TestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.kubernetes.crds.jmeter.Jmeter;
import io.metersphere.engine.kubernetes.crds.jmeter.JmeterSpec;
import io.metersphere.engine.kubernetes.provider.ClientCredential;
import io.metersphere.engine.kubernetes.provider.KubernetesProvider;
import io.metersphere.service.LoadTestService;
import io.metersphere.service.TestResourcePoolService;
import io.metersphere.service.TestResourceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

public class KubernetesTestEngine implements Engine {
    private FileMetadata jmxFile;
    private List<FileMetadata> csvFiles;
    private LoadTestWithBLOBs loadTest;
    private LoadTestService loadTestService;
    private Integer threadNum;

    private String resourcePoolId;
    private List<TestResource> resourceList;

    @Override
    public boolean init(LoadTestWithBLOBs loadTest, FileMetadata fileMetadata, List<FileMetadata> csvFiles) {
        this.loadTest = loadTest;
        this.jmxFile = fileMetadata;
        this.csvFiles = csvFiles;
        TestResourcePoolService testResourcePoolService = CommonBeanFactory.getBean(TestResourcePoolService.class);
        TestResourceService testResourceService = CommonBeanFactory.getBean(TestResourceService.class);
        this.loadTestService = CommonBeanFactory.getBean(LoadTestService.class);

        String loadConfiguration = loadTest.getLoadConfiguration();
        JSONArray jsonArray = JSON.parseArray(loadConfiguration);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            if (StringUtils.equals("resourcePoolId", o.getString("key"))) {
                resourcePoolId = o.getString("value");
            }
            if (StringUtils.equals(o.getString("key"), "TargetLevel")) {
                threadNum = o.getInteger("value");
            }
        }
        if (StringUtils.isBlank(resourcePoolId)) {
            MSException.throwException("Resource Pool ID is empty");
        }
        TestResourcePool resourcePool = testResourcePoolService.getResourcePool(resourcePoolId);
        if (resourcePool == null) {
            MSException.throwException("Resource Pool is empty");
        }
        if (!ResourcePoolTypeEnum.K8S.name().equals(resourcePool.getType())) {
            MSException.throwException("Invalid Resource Pool type.");
        }
        this.resourceList = testResourceService.getResourcesByPoolId(resourcePool.getId());
        if (CollectionUtils.isEmpty(this.resourceList)) {
            MSException.throwException("Test Resource is empty");
        }
        return true;
    }


    @Override
    public void start() {
        // resourceList size 1
        List<LoadTestWithBLOBs> loadTests = loadTestService.selectByTestResourcePoolId(resourcePoolId);
        // 使用当前资源池正在运行的测试占用的并发数
        Integer sumThreadNum = loadTests.stream().filter(t -> TestStatus.Running.name().equals(t.getStatus())).map(t -> {
            Integer s = 0;
            String loadConfiguration = t.getLoadConfiguration();
            JSONArray jsonArray = JSON.parseArray(loadConfiguration);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                if (StringUtils.equals(o.getString("key"), "TargetLevel")) {
                    s = o.getInteger("value");
                }
            }
            return s;
        }).reduce(Integer::sum).orElse(0);
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
                EngineContext context = EngineFactory.createContext(loadTest, jmxFile, csvFiles);
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
