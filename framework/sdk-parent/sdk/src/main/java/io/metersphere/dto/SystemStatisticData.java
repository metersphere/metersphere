package io.metersphere.dto;

import lombok.Data;

@Data
public class SystemStatisticData {
    private long userSize = 0;
    private long workspaceSize = 0;
    private long projectSize = 0;
}
