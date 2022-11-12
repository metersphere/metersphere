package io.metersphere.engine;

import io.metersphere.base.domain.LoadTestReportResult;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.mapper.LoadTestReportResultMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportKeys;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LocalAddressUtils;
import io.metersphere.commons.utils.UrlTestUtils;
import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaProperties;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.request.StartTestRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.service.BaseTestResourcePoolService;
import io.metersphere.service.BaseTestResourceService;
import io.metersphere.service.PerformanceTestService;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class AbstractEngine implements Engine {
    protected String JMETER_IMAGE;
    protected String HEAP;
    protected String GC_ALGO;
    protected LoadTestReportWithBLOBs loadTestReport;
    protected PerformanceTestService performanceTestService;
    protected Integer threadNum;
    protected List<TestResource> resourceList;
    protected TestResourcePool resourcePool;
    private final BaseTestResourcePoolService baseTestResourcePoolService;
    private final BaseTestResourceService baseTestResourceService;

    public AbstractEngine() {
        baseTestResourcePoolService = CommonBeanFactory.getBean(BaseTestResourcePoolService.class);
        baseTestResourceService = CommonBeanFactory.getBean(BaseTestResourceService.class);
        JMETER_IMAGE = CommonBeanFactory.getBean(JmeterProperties.class).getImage();
        HEAP = CommonBeanFactory.getBean(JmeterProperties.class).getHeap();
        GC_ALGO = CommonBeanFactory.getBean(JmeterProperties.class).getGcAlgo();
    }

    protected void init(LoadTestReportWithBLOBs loadTestReport) {
        if (loadTestReport == null) {
            MSException.throwException("LoadTest is null.");
        }
        this.loadTestReport = loadTestReport;

        this.performanceTestService = CommonBeanFactory.getBean(PerformanceTestService.class);

        threadNum = getThreadNum(loadTestReport);
        String resourcePoolId = loadTestReport.getTestResourcePoolId();
        if (StringUtils.isBlank(resourcePoolId)) {
            MSException.throwException("Resource Pool ID is empty");
        }
        resourcePool = baseTestResourcePoolService.getResourcePool(resourcePoolId);
        if (resourcePool == null || StringUtils.equals(resourcePool.getStatus(), ResourceStatusEnum.DELETE.name())) {
            MSException.throwException("Resource Pool is empty");
        }
        if (!ResourcePoolTypeEnum.K8S.name().equals(resourcePool.getType())
                && !ResourcePoolTypeEnum.NODE.name().equals(resourcePool.getType())) {
            MSException.throwException("Invalid Resource Pool type.");
        }
        if (!StringUtils.equals(resourcePool.getStatus(), ResourceStatusEnum.VALID.name())) {
            MSException.throwException("Resource Pool Status is not VALID");
        }
        if (!BooleanUtils.toBoolean(resourcePool.getPerformance())) {
            MSException.throwException("Resource Pool performance not supported");
        }
        // image
        String image = resourcePool.getImage();
        if (StringUtils.isNotEmpty(image)) {
            JMETER_IMAGE = image;
        }
        // heap
        String heap = resourcePool.getHeap();
        if (StringUtils.isNotEmpty(heap)) {
            HEAP = heap;
        }
        // gc_algo
        String gcAlgo = resourcePool.getGcAlgo();
        if (StringUtils.isNotEmpty(gcAlgo)) {
            GC_ALGO = gcAlgo;
        }
        this.resourceList = baseTestResourceService.getResourcesByPoolId(resourcePool.getId());
        if (CollectionUtils.isEmpty(this.resourceList)) {
            MSException.throwException("Test Resource is empty");
        }
    }

    protected Integer getRunningThreadNum() {
        List<LoadTestReportWithBLOBs> loadTestReports = performanceTestService.selectReportsByTestResourcePoolId(loadTestReport.getTestResourcePoolId());
        // 使用当前资源池正在运行的测试占用的并发数
        return loadTestReports.stream()
                .filter(t -> PerformanceTestStatus.Running.name().equals(t.getStatus()))
                .map(this::getThreadNum)
                .reduce(Integer::sum)
                .orElse(0);
    }

    private Integer getThreadNum(LoadTestReportWithBLOBs t) {
        Integer s = 0;
        String loadConfiguration = t.getLoadConfiguration();
        List jsonArray = JSON.parseArray(loadConfiguration);

        Iterator<Object> iterator = jsonArray.iterator();
        outer:
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof List) {
                List<Object> o = (List<Object>) next;
                for (Object o1 : o) {
                    Map jsonObject = (Map) o1;
                    String key = (String) jsonObject.get("key");
                    if (StringUtils.equals(key, "deleted")) {
                        String value = (String) jsonObject.get("value");
                        if (StringUtils.equals(value, "true")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
                for (Object o1 : o) {
                    Map jsonObject = (Map) o1;
                    String key = (String) jsonObject.get("key");
                    if (StringUtils.equals(key, "enabled")) {
                        String value = (String) jsonObject.get("value");
                        if (StringUtils.equals(value, "false")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
            }
        }

        for (Object value : jsonArray) {
            if (value instanceof List) {
                List o = (List) value;
                for (Object item : o) {
                    Map m = (Map) item;
                    if (StringUtils.equals((String) m.get("key"), "TargetLevel")) {
                        s += (Integer) m.get("value");
                        break;
                    }
                }
            }
        }
        return s;
    }

    protected StartTestRequest createStartTestRequest(TestResource resource, Object[] ratios, int resourceIndex) {
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        KafkaProperties kafkaProperties = CommonBeanFactory.getBean(KafkaProperties.class);
        String metersphereUrl = "http://localhost:8081";
        if (baseInfo != null) {
            metersphereUrl = baseInfo.getUrl();
        }
        // docker 不能从 localhost 中下载文件, 本地开发
        if (StringUtils.contains(metersphereUrl, "http://localhost") ||
                StringUtils.contains(metersphereUrl, "http://127.0.0.1")) {
            metersphereUrl = "http://" + LocalAddressUtils.getIpAddress("en0") + ":8081";
        }

        String jmeterPingUrl = metersphereUrl + "/jmeter/ping"; // 检查下载地址是否正确
        if (!UrlTestUtils.testUrlWithTimeOut(jmeterPingUrl, 1000)) {
            MSException.throwException(Translator.get("run_load_test_file_init_error"));
        }

        Map<String, String> env = new HashMap<>();
        env.put("RATIO", StringUtils.join(ratios, ","));
        env.put("RESOURCE_INDEX", StringUtils.EMPTY + resourceIndex);
        env.put("METERSPHERE_URL", metersphereUrl);
        env.put("START_TIME", StringUtils.EMPTY + System.currentTimeMillis());
        env.put("TEST_ID", this.loadTestReport.getTestId());
        env.put("REPORT_ID", this.loadTestReport.getId());
        env.put("BOOTSTRAP_SERVERS", kafkaProperties.getBootstrapServers());
        env.put("LOG_TOPIC", kafkaProperties.getLog().getTopic());
        env.put("JMETER_REPORTS_TOPIC", kafkaProperties.getReport().getTopic());
        env.put("RESOURCE_ID", resource.getId());
        env.put("THREAD_NUM", "0"); // 不用修改线程数
        env.put("HEAP", HEAP);
        env.put("GC_ALGO", GC_ALGO);
        env.put("GRANULARITY", performanceTestService.getGranularity(this.loadTestReport.getId()).toString());
        env.put("BACKEND_LISTENER", resourcePool.getBackendListener().toString());

        StartTestRequest request = new StartTestRequest();
        request.setImage(JMETER_IMAGE);
        request.setEnv(env);
        return request;
    }

    protected void saveCompleteCount(Object[] ratios) {
        LoadTestReportResult completeCount = new LoadTestReportResult();
        completeCount.setId(UUID.randomUUID().toString());
        completeCount.setReportId(loadTestReport.getId());
        completeCount.setReportKey(ReportKeys.ReportCompleteCount.name());
        completeCount.setReportValue(StringUtils.EMPTY + ratios.length); // 初始化一个 completeCount, 这个值用在data-streaming中
        LoadTestReportResultMapper loadTestReportResultMapper = CommonBeanFactory.getBean(LoadTestReportResultMapper.class);
        loadTestReportResultMapper.insertSelective(completeCount);
    }

}
