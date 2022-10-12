package io.metersphere.api.exec.engine;

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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public abstract class AbstractEngine implements Engine {
    protected String JMETER_IMAGE;
    protected String HEAP;
    protected String GC_ALGO;
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
}
