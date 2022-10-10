package io.metersphere.xpack.resourcepool.service;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.service.BaseTestResourcePoolService;
import io.metersphere.xpack.quota.service.QuotaService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ValidQuotaResourcePoolService {
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;

    public List<TestResourcePoolDTO> listValidQuotaResourcePools() {
        return filterQuota(baseTestResourcePoolService.listValidResourcePools());
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
}
