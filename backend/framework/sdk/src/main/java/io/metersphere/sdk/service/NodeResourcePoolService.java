package io.metersphere.sdk.service;


import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.TestResourceDTO;
import io.metersphere.sdk.dto.TestResourceNodeDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class NodeResourcePoolService {

    private final static String nodeControllerUrl = "http://%s:%s/status";

    private static final RestTemplate restTemplateWithTimeOut = new RestTemplate();
    static {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(2000);
        httpRequestFactory.setConnectTimeout(2000);
        restTemplateWithTimeOut.setRequestFactory(httpRequestFactory);
    }

    public boolean validate(TestResourceDTO testResourceDTO, Boolean usedApiType) {
        List<TestResourceNodeDTO> nodesList = testResourceDTO.getNodesList();
        if (CollectionUtils.isEmpty(nodesList)) {
            throw new MSException(Translator.get("no_nodes_message"));
        }
        boolean isValid = true;
        for (TestResourceNodeDTO testResourceNodeDTO : nodesList) {
            if (StringUtils.isBlank(testResourceNodeDTO.getIp())) {
                throw new MSException(Translator.get("ip_is_null"));
            }
            if (StringUtils.isBlank(testResourceNodeDTO.getPort())) {
                throw new MSException(Translator.get("port_is_null"));
            }
            if (testResourceNodeDTO.getConcurrentNumber() == null) {
                throw new MSException(Translator.get("concurrent_number_is_null"));
            }
            if (!usedApiType) {
                if (StringUtils.isBlank(testResourceNodeDTO.getMonitor())) {
                    throw new MSException(Translator.get("monitor_is_null"));
                }
            }
            isValid = validateNode(testResourceNodeDTO);
            if (!isValid) {
                 break;
            }
        }
        //校验节点
        List<ImmutablePair<String, String>> ipPort = nodesList.stream()
                .map(resource -> {
                    return new ImmutablePair<>(resource.getIp(), resource.getPort());
                })
                .distinct()
                .toList();
        if (ipPort.size() < nodesList.size()) {
            throw new MSException(Translator.get("duplicate_node_ip_port"));
        }
        return isValid;
    }

    private boolean validateNode(TestResourceNodeDTO node) {
        try {
            ResponseEntity<ResultHolder> entity = restTemplateWithTimeOut.getForEntity(String.format(nodeControllerUrl, node.getIp(), node.getPort()), ResultHolder.class);
            ResultHolder body = entity.getBody();
            if (body == null) {
                return false;
            }
            if (body.getData() != null && StringUtils.equalsIgnoreCase("OK", body.getData().toString())) {
                return true;
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
        return false;
    }
}
