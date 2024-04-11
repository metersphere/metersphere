package io.metersphere.system.dto;

import io.metersphere.system.domain.OperationHistory;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: LAN
 * @date: 2024/1/2 19:03
 * @version: 1.0
 */
@Data
public class OperationHistoryDTO extends OperationHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 操作人
    private String createUserName;

    // 版本
    private String versionName;

    //是否是最新
    private boolean isLatest;
}
