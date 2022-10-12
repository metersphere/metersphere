package io.metersphere.api.exec.engine;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.JmeterProperties;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.engine.Engine;
import io.metersphere.service.BaseTestResourcePoolService;
import io.metersphere.service.BaseTestResourceService;
import io.metersphere.commons.utils.JSONUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractEngine implements Engine {
    protected String JMETER_IMAGE;
    protected String HEAP;
    protected String GC_ALGO;
    protected LoadTestReportWithBLOBs loadTestReport;
    protected Integer threadNum;
    protected List<TestResource> resourceList;
    protected TestResourcePool resourcePool;
    private final BaseTestResourcePoolService testResourcePoolService;
    private final BaseTestResourceService testResourceService;

    public AbstractEngine() {
        testResourcePoolService = CommonBeanFactory.getBean(BaseTestResourcePoolService.class);
        testResourceService = CommonBeanFactory.getBean(BaseTestResourceService.class);
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


    private Integer getThreadNum(LoadTestReportWithBLOBs t) {
        Integer s = 0;
        String loadConfiguration = t.getLoadConfiguration();
        JSONArray jsonArray = JSONUtil.parseArray(loadConfiguration);

        Iterator<Object> iterator = jsonArray.iterator();
        outer:
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof List) {
                List<Object> o = (List<Object>) next;
                for (Object o1 : o) {
                    JSONObject jsonObject = JSONUtil.parseObject(o1.toString());
                    String key = jsonObject.optString("key");
                    if (StringUtils.equals(key, "deleted")) {
                        String value = jsonObject.optString("value");
                        if (StringUtils.equals(value, "true")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
                for (Object o1 : o) {
                    JSONObject jsonObject = JSONUtil.parseObject(o1.toString());
                    String key = jsonObject.optString("key");
                    if (StringUtils.equals(key, "enabled")) {
                        String value = jsonObject.optString("value");
                        if (StringUtils.equals(value, "false")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.get(i) instanceof List) {
                JSONArray o = jsonArray.getJSONArray(i);
                for (int j = 0; j < o.length(); j++) {
                    if (StringUtils.equals(o.optJSONObject(j).optString("key"), "TargetLevel")) {
                        s += o.optJSONObject(j).optInt("value");
                        break;
                    }
                }
            }
        }
        return s;
    }
}
