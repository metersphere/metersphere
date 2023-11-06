package io.metersphere.api.engine;

import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.service.TestResourcePoolService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

public abstract class AbstractApiEngine implements ApiEngine {
    // 资源池
    protected TestResourcePoolReturnDTO resourcePool;
    private final TestResourcePoolService testResourcePoolService;

    public AbstractApiEngine() {
        resourcePool = new TestResourcePoolReturnDTO();
        testResourcePoolService = CommonBeanFactory.getBean(TestResourcePoolService.class);
    }

    protected void initConfig(TaskRequest taskRequest) {
        LogUtils.info("初始化参数", taskRequest.getReportId());
        String resourcePoolId = taskRequest.getPoolId();
        resourcePool = testResourcePoolService.getTestResourcePoolDetail(resourcePoolId);

        if (resourcePool == null || BooleanUtils.isTrue(resourcePool.getDeleted())) {
            throw new MSException("Resource Pool is empty");
        }

        if (!ResourcePoolTypeEnum.K8S.name().equals(resourcePool.getType())
                && !ResourcePoolTypeEnum.NODE.name().equals(resourcePool.getType())) {
            throw new MSException("Invalid Resource Pool type.");
        }

        if (BooleanUtils.isFalse(resourcePool.getEnable())) {
            throw new MSException("Resource Pool Status is not VALID");
        }

        if (resourcePool.getTestResourceReturnDTO() == null ||
                CollectionUtils.isEmpty(resourcePool.getTestResourceReturnDTO().getNodesList())) {
            throw new MSException("Test Resource is empty");
        }

        LogUtils.info("初始化参数完成", taskRequest.getReportId());
    }
}
