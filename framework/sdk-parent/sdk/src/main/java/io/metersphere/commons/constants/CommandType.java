package io.metersphere.commons.constants;

/**
 * atomic 原子指令，前端 后端 一对一
 * combine 前端指令包含了多个指令实现 比如 IF 指令，if 指令必须有 start 和 end
 * proxy 前端指令使用了代理模式 比如前端选择等待元素指令，实际上还有一个子类别具体选择，waitForElement 还是 waitForEditable
 */
public class CommandType {
    public static final String COMMAND_TYPE_ATOM = "atom";
    public static final String COMMAND_TYPE_COMBINATION = "combination";
    public static final String COMMAND_TYPE_PROXY = "proxy";
    public static final String COMMAND_TYPE_COMBINATION_PROXY = "combination_proxy";
}
