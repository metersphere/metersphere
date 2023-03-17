package io.metersphere.commons.utils;

import io.metersphere.api.dto.ApiMockEnvUpdateDTO;
import java.util.function.Consumer;

public class BatchProcessingUtil {
    public static final int BATCH_PROCESS_QUANTITY = 1000;

    public static void batchUpdateMockEnvConfig(String oldUrl,String newUrl,long mockEnvCount, Consumer<ApiMockEnvUpdateDTO> func){
        long remainderMockEnvCount = mockEnvCount;
        int startPage = 0;
        while (remainderMockEnvCount > 0 ){
            ApiMockEnvUpdateDTO dto = new ApiMockEnvUpdateDTO();
            dto.setOldBaseUrl(oldUrl);
            dto.setBaseUrl(newUrl);
            dto.setLimitSize(BATCH_PROCESS_QUANTITY);
            dto.setLimitStart(startPage);
            func.accept(dto);
            startPage = startPage + BATCH_PROCESS_QUANTITY;
            remainderMockEnvCount = remainderMockEnvCount - BATCH_PROCESS_QUANTITY;
        }
    }
}
