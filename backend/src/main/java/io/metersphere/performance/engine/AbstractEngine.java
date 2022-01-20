package io.metersphere.performance.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.JmeterProperties;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.TestResourcePoolService;
import io.metersphere.service.TestResourceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractEngine implements Engine {
    protected String JMETER_IMAGE;
    protected String HEAP;
    protected String GC_ALGO;
    protected LoadTestReportWithBLOBs loadTestReport;
    protected PerformanceTestService performanceTestService;
    protected Integer threadNum;
    protected List<TestResource> resourceList;
    protected TestResourcePool resourcePool;
    private final TestResourcePoolService testResourcePoolService;
    private final TestResourceService testResourceService;

    public AbstractEngine() {
        testResourcePoolService = CommonBeanFactory.getBean(TestResourcePoolService.class);
        testResourceService = CommonBeanFactory.getBean(TestResourceService.class);
        JMETER_IMAGE = CommonBeanFactory.getBean(JmeterProperties.class).getImage();
        HEAP = CommonBeanFactory.getBean(JmeterProperties.class).getHeap();
        GC_ALGO = CommonBeanFactory.getBean(JmeterProperties.class).getGcAlgo();
    }

    protected void initApiConfig(JmeterRunRequestDTO runRequest) {
        String resourcePoolId = runRequest.getPoolId();
        resourcePool = testResourcePoolService.getResourcePool(resourcePoolId);
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
        this.resourceList = testResourceService.getResourcesByPoolId(resourcePool.getId());
        if (CollectionUtils.isEmpty(this.resourceList)) {
            MSException.throwException("Test Resource is empty");
        }
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
        resourcePool = testResourcePoolService.getResourcePool(resourcePoolId);
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
        this.resourceList = testResourceService.getResourcesByPoolId(resourcePool.getId());
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
        JSONArray jsonArray = JSON.parseArray(loadConfiguration);

        Iterator<Object> iterator = jsonArray.iterator();
        outer:
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof List) {
                List<Object> o = (List<Object>) next;
                for (Object o1 : o) {
                    JSONObject jsonObject = JSONObject.parseObject(o1.toString());
                    String key = jsonObject.getString("key");
                    if (StringUtils.equals(key, "deleted")) {
                        String value = jsonObject.getString("value");
                        if (StringUtils.equals(value, "true")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
                for (Object o1 : o) {
                    JSONObject jsonObject = JSONObject.parseObject(o1.toString());
                    String key = jsonObject.getString("key");
                    if (StringUtils.equals(key, "enabled")) {
                        String value = jsonObject.getString("value");
                        if (StringUtils.equals(value, "false")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i) instanceof List) {
                JSONArray o = jsonArray.getJSONArray(i);
                for (int j = 0; j < o.size(); j++) {
                    if (StringUtils.equals(o.getJSONObject(j).getString("key"), "TargetLevel")) {
                        s += o.getJSONObject(j).getInteger("value");
                        break;
                    }
                }
            }
        }
        return s;
    }
}
