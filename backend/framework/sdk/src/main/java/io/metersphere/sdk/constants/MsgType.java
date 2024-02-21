package io.metersphere.sdk.constants;

public enum MsgType {
    CONNECT,   // 链接标识
    HEARTBEAT, // 心跳检查标识
    EXEC_START, // 开始执行标识
    EXEC_RESULT, // 执行结果标识
    EXEC_END // 执行结束
}
