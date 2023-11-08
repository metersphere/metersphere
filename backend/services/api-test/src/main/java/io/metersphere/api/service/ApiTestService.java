package io.metersphere.api.service;

import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.service.ApiPluginService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestService {

    @Resource
    private ApiPluginService apiPluginService;

    public List<ProtocolDTO> getProtocols(String orgId) {
        return apiPluginService.getProtocols(orgId);
    }

}