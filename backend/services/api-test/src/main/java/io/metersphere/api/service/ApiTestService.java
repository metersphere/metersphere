package io.metersphere.api.service;

import io.metersphere.api.dto.request.http.MsHTTPElement;
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

    private static final String HTTP_PROTOCOL = "HTTP";
    @Resource
    private ApiPluginService apiPluginService;

    public List<ProtocolDTO> getProtocols(String orgId) {
        List<ProtocolDTO> protocols = apiPluginService.getProtocols(orgId);
        // 将 http 协议放最前面
        ProtocolDTO protocolDTO = new ProtocolDTO();
        protocolDTO.setProtocol(HTTP_PROTOCOL);
        protocolDTO.setPolymorphicName(MsHTTPElement.class.getSimpleName());
        protocols.addFirst(protocolDTO);
        return protocols;
    }

}