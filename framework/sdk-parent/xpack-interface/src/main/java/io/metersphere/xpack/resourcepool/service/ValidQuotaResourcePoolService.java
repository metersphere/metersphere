package io.metersphere.xpack.resourcepool.service;

import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.quota.service.BaseQuotaService;
import io.metersphere.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.service.BaseTestResourcePoolService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.ResourceStatusEnum.VALID;

@Service
public class ValidQuotaResourcePoolService {
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;
    @Resource
    private BaseQuotaService baseQuotaService;

    public List<TestResourcePoolDTO> listValidQuotaResourcePools() {
        return filterQuota(listValidResourcePools());
    }
    private List<TestResourcePoolDTO> filterQuota(List<TestResourcePoolDTO> list) {
        String projectId = SessionUtils.getCurrentProjectId();
        Set<String> pools = baseQuotaService.getQuotaResourcePools(projectId);
        if (!pools.isEmpty()) {
            return list.stream().filter(pool -> pools.contains(pool.getId())).collect(Collectors.toList());
        }
        return list;
    }

    public List<TestResourcePoolDTO> listValidResourcePools() {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setStatus(VALID.name());
        return baseTestResourcePoolService.listResourcePools(request);
    }
}
