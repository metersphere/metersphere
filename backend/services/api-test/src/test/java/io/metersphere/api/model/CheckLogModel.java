package io.metersphere.api.model;

/**
 * @author: LAN
 * @date: 2023/12/15 14:55
 * @version: 1.0
 */

import io.metersphere.system.log.constants.OperationLogType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckLogModel {
    private String resourceId;
    private OperationLogType operationType;
    private String url;
}
