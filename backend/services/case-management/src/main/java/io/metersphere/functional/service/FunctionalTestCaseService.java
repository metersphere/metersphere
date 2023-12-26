package io.metersphere.functional.service;

import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.request.ApiTestCasePageProviderRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author guoyuqi
 * 功能用例关联其他用例服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalTestCaseService {

    @Resource
    private BaseAssociateApiProvider provider;

    public List<ApiTestCaseProviderDTO> page(ApiTestCasePageProviderRequest request) {
        return provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
    }
}
