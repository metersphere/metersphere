package io.metersphere.xpack.resourcepool.service;

import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.service.BaseTestResourcePoolService;
import io.metersphere.service.NodeResourcePoolService;
import io.metersphere.xpack.quota.service.QuotaService;
import io.metersphere.xpack.resourcepool.engine.KubernetesResourcePoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.ResourceStatusEnum.INVALID;
import static io.metersphere.commons.constants.ResourceStatusEnum.VALID;

@Service
public class ValidQuotaResourcePoolService {
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;
    @Resource
    private NodeResourcePoolService nodeResourcePoolService;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;

    public List<TestResourcePoolDTO> listValidQuotaResourcePools() {
        return filterQuota(listValidResourcePools());
    }

    private List<TestResourcePoolDTO> filterQuota(List<TestResourcePoolDTO> list) {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService == null) {
            return list;
        }
        Set<String> pools = quotaService.getQuotaResourcePools();
        if (!pools.isEmpty()) {
            return list.stream().filter(pool -> pools.contains(pool.getId())).collect(Collectors.toList());
        }
        return list;
    }


    private boolean validateTestResourcePool(TestResourcePoolDTO testResourcePool) {
        if (StringUtils.equalsIgnoreCase(testResourcePool.getType(), ResourcePoolTypeEnum.K8S.name())) {
            KubernetesResourcePoolService resourcePoolService = CommonBeanFactory.getBean(KubernetesResourcePoolService.class);
            if (resourcePoolService == null) {
                return false;
            }
            return resourcePoolService.validate(testResourcePool);
        }
        return nodeResourcePoolService.validate(testResourcePool);
    }


    public List<TestResourcePoolDTO> listValidResourcePools() {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        List<TestResourcePoolDTO> testResourcePools = baseTestResourcePoolService.listResourcePools(request);
        // 重新校验 pool
        for (TestResourcePoolDTO pool : testResourcePools) {
            // 手动设置成无效的, 排除
            if (INVALID.name().equals(pool.getStatus())) {
                continue;
            }
            try {
                validateTestResourcePool(pool);
            } catch (Throwable e) {
                LogUtil.error(e.getMessage(), e);
                pool.setStatus(INVALID.name());
                pool.setUpdateTime(System.currentTimeMillis());
                testResourcePoolMapper.updateByPrimaryKeySelective(pool);
            }
        }
        request.setStatus(VALID.name());
        return baseTestResourcePoolService.listResourcePools(request);
    }
}
