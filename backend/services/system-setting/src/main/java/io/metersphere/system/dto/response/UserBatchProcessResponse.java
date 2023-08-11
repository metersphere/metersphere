package io.metersphere.system.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserBatchProcessResponse {
    private long totalCount;
    private long successCount;
    private List<String> processedIds;
}
