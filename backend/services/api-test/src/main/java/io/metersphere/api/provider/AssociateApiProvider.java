package io.metersphere.api.provider;

import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.dto.ApiTestCaseProviderDTO;
import io.metersphere.provider.BaseAssociateApiProvider;
import io.metersphere.request.ApiTestCasePageProviderRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AssociateApiProvider implements BaseAssociateApiProvider {
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Override
    public List<ApiTestCaseProviderDTO> getApiTestCaseList(String sourceType, String sourceName, String targetName, ApiTestCasePageProviderRequest apiTestCasePageProviderRequest) {
        return extApiTestCaseMapper.listByProviderRequest(sourceType, sourceName, targetName, apiTestCasePageProviderRequest, false);
    }
}
