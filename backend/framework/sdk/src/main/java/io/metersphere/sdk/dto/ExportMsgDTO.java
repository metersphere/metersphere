package io.metersphere.sdk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ExportMsgDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * fileId
     */
    private String fileId;
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 数量
     */
    private int count;
    /**
     * 导出状态
     */
    private boolean isSuccessful;

    /**
     * 消息类型（CONNECT-链接标识，HEARTBEAT-心跳检查标识，EXEC_START-开始执行标识，EXEC_RESULT-执行结果标识）
     */
    private String msgType;
}
