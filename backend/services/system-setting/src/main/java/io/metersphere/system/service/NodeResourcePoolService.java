package io.metersphere.system.service;


import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.sdk.LicenseDTO;
import io.metersphere.system.utils.TaskRunnerClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class NodeResourcePoolService {

    private final static String nodeControllerUrl = "http://%s:%s/status";


    public boolean validate(TestResourceDTO testResourceDTO) {
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
            if (!licenseValidate()) {
                if (testResourceNodeDTO.getConcurrentNumber() > 10) {
                    testResourceNodeDTO.setConcurrentNumber(10);
                }
                if (testResourceNodeDTO.getSingleTaskConcurrentNumber() > 3) {
                    testResourceNodeDTO.setSingleTaskConcurrentNumber(3);
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

    private boolean licenseValidate() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            LicenseDTO licenseDTO = licenseService.validate();
            return (licenseDTO != null && StringUtils.equals(licenseDTO.getStatus(), "valid"));
        } else {
            return false;
        }
    }

    public boolean validateNode(TestResourceNodeDTO node) {
        try {
            ResultHolder body = TaskRunnerClient.get(String.format(nodeControllerUrl, node.getIp(), node.getPort()));
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
