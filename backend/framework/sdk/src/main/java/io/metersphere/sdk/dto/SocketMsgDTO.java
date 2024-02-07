package io.metersphere.sdk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SocketMsgDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 报告ID
     */
    private String reportId;
    /**
     * 执行模式
     */
    private String runMode;
    /**
     * 消息类型（LINK-链接标识，HEARTBEAT-心跳检查标识，EXEC_START-开始执行标识，EXEC_RESULT-执行结果标识）
     */
    private String msgType;
    /**
     * 结果内容
     */
    private Object taskResult;

}
