package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugSyncNoticeDTO {

    @Schema(description ="message.domain.bug_sync_platform")
    private String platform;

    @Schema(description ="message.domain.bug_sync_total_count")
    private Integer total;

    @Schema(description ="message.domain.triggerMode")
    private String triggerMode;
}
