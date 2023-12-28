package io.metersphere.functional.service;

import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.request.ApiModuleProviderRequest;
import io.metersphere.request.ApiTestCasePageProviderRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author guoyuqi
 * 功能用例关联其他用例服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalTestCaseService {

    @Resource
    private BaseAssociateApiProvider provider;

    /**
     * 获取功能用例未关联的接口用例列表
     * @param request request
     * @return List<ApiTestCaseProviderDTO>
     */
    public List<ApiTestCaseProviderDTO> page(ApiTestCasePageProviderRequest request) {
        return provider.getApiTestCaseList("functional_case_test", "case_id", "source_id", request);
    }

    /**
     * 根据接口用例的搜索条件获取符合条件的接口定义的模块统计数量
     *
     * @param request 接口用例高级搜索条件
     * @param deleted 接口定义是否删除
     * @return 接口模块统计数量
     */
    public Map<String, Long> moduleCount(ApiModuleProviderRequest request, boolean deleted) {
        return provider.moduleCount("functional_case_test", "case_id", "source_id", request, deleted);

    }
}
