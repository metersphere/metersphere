package io.metersphere.sdk.exception;

import io.metersphere.sdk.util.Translator;

/**
 * API 接口状态码
 * @author jianxing
 *
 * 1. 如果想返回具有 Http 含义的状态码，使用 MsHttpResultCode
 * 2. 业务状态码，各模块定义自己的状态码枚举类，各自管理
 * 3. 业务错误码，定义规则为 6 位数字
 * 4. 业务错误码，前三位表示模块名称，最后三位表示错误码。例如：101001  101: 系统设置， 001: 资源池校验失败
 * 5. 当需要抛出异常时，给异常设置状态码枚举对象
 *
 * 业务状态码列表：
 *  100: 通用功能
 *  101: 系统设置
 *  102: 项目设置
 *  104: 接口测试
 *  105: 测试跟踪
 *  107: 测试计划
 *  108: 缺陷管理
 *  109: 工作台
 */
public interface IResultCode {
    /**
     * 返回状态码
     */
    int getCode();

    /**
     * 返回状态码信息
     */
    String getMessage();

    /**
     * 返回国际化后的状态码信息
     * 如果没有匹配则返回原文
     */
    default String getTranslationMessage(String message) {
       return Translator.get(message, message);
    }
}
