package io.metersphere.system.service;

import io.metersphere.system.dto.pool.TestResourceNodeDTO;

import java.util.List;

public interface TestResourcePoolValidateService {

    void validateNodeList(List<TestResourceNodeDTO> nodesList);

}
