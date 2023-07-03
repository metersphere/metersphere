package io.metersphere.system.dto.response;

import lombok.Data;

@Data
public class UserBatchProcessResponse {
    private long totalCount;
    private long successCount;
}
