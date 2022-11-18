package io.metersphere.engine;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.request.StartTestRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.xpack.resourcepool.engine.provider.ClientCredential;
import io.metersphere.xpack.resourcepool.engine.provider.KubernetesProvider;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Set;

public class KubernetesTestEngine extends AbstractEngine {

    private Environment env;
    private JmeterRunRequestDTO runRequest;
    private static Class<? extends KubernetesProvider> providerClass = null;

    static {
        Set<Class<? extends KubernetesProvider>> subTypes = new Reflections("io.metersphere.xpack").getSubTypesOf(KubernetesProvider.class);
        if (CollectionUtils.isNotEmpty(subTypes)) {
            providerClass = subTypes.stream().findFirst().get();
        }
    }

    public KubernetesTestEngine(LoadTestReportWithBLOBs loadTestReport) {
        this.init(loadTestReport);
    }


    @Override
    public void init(LoadTestReportWithBLOBs loadTestReport) {
        super.init(loadTestReport);
        env = CommonBeanFactory.getBean(Environment.class);
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
            Integer podThreadLimit = clientCredential.getPodThreadLimit();
            if (podThreadLimit == null || podThreadLimit == 0) {
                podThreadLimit = Integer.valueOf(env.getProperty("jmeter.pod.threads.limit", "5000"));
            }
            // 当前测试需要的并发数大于剩余的并发数报错
            if (threadNum > maxConcurrency - sumThreadNum) {
                MSException.throwException(Translator.get("max_thread_insufficient"));
            }
            try {
                int remainThreadNum = threadNum;

                double podNum = Math.ceil((double) remainThreadNum / podThreadLimit);
                Object[] ratios = new Object[(int) podNum];
                Arrays.fill(ratios, String.format("%.2f", 1.0 / podNum));
                // 保存一个 completeCount
                saveCompleteCount(ratios);
                for (int i = 0; i < podNum; i++) {
                    runTest(r, ratios, i);
                }
            } catch (MSException e) {
                throw e;
            } catch (Exception e) {
                MSException.throwException(e);
            }
        });

    }


    private void runTest(TestResource resource, Object[] ratios, int resourceIndex) {
        StartTestRequest request = createStartTestRequest(resource, ratios, resourceIndex);

        ClientCredential clientCredential = JSON.parseObject(resource.getConfiguration(), ClientCredential.class);

        try {
            KubernetesProvider kubernetesProvider = ConstructorUtils.invokeConstructor(providerClass, clientCredential);
            kubernetesProvider.deployJmeter(request, clientCredential);
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException("kubernetes deploy jmeter error please check the yaml file");
        }
    }


    @Override
    public void stop() {
        String testId = this.loadTestReport.getTestId();
        resourceList.forEach(r -> {
            ClientCredential clientCredential = JSON.parseObject(r.getConfiguration(), ClientCredential.class);
            try {
                KubernetesProvider kubernetesProvider = ConstructorUtils.invokeConstructor(providerClass, clientCredential);
                kubernetesProvider.deleteJmeter(testId, clientCredential.getNamespace());
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
    }
}
