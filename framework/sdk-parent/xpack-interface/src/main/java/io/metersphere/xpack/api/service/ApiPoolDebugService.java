package io.metersphere.xpack.api.service;

import io.metersphere.base.domain.TestResource;
import io.metersphere.dto.JmeterRunRequestDTO;

import java.util.List;

public interface ApiPoolDebugService {
    public void run(JmeterRunRequestDTO request, List<TestResource> resources);
}
