package io.metersphere.environment.utils;


import io.metersphere.environment.dto.ApiMockEnvUpdateDTO;

import java.util.function.Consumer;

public class BatchProcessingUtil {
    public static final int BATCH_PROCESS_QUANTITY = 1000;

    public static void batchUpdateMockEnvConfig(String newUrl, long mockEnvCount, Consumer<ApiMockEnvUpdateDTO> func) {
        long remainderMockEnvCount = mockEnvCount;
        int startPage = 0;
        while (remainderMockEnvCount > 0) {
            ApiMockEnvUpdateDTO dto = new ApiMockEnvUpdateDTO();
            dto.setBaseUrl(newUrl);
            dto.setLimitSize(BATCH_PROCESS_QUANTITY);
            dto.setLimitStart(startPage);
            func.accept(dto);
            startPage = startPage + BATCH_PROCESS_QUANTITY;
            remainderMockEnvCount = remainderMockEnvCount - BATCH_PROCESS_QUANTITY;
        }
    }
}
