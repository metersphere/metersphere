package io.metersphere.api.exec;

import io.metersphere.dto.JmeterRunRequestDTO;

public interface ApiPoolDebugService {
    public void run(JmeterRunRequestDTO request);
}
