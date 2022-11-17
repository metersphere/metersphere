package io.metersphere.api.exec;

import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.RunModeConfigDTO;

public interface ApiPoolDebugService {
    public void run(JmeterRunRequestDTO request);

    public void verifyPool(String projectId, RunModeConfigDTO runConfig);
}
