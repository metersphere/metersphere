package io.metersphere.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class BugSyncResult implements Serializable {

    @Schema(description = "是否同步完成")
    private Boolean complete;

    @Schema(description = "是否同步完成")
    private String msg;
}
