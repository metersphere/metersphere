package io.metersphere.api.parser;

import io.metersphere.api.parser.jmeter.JmeterTestElementParser;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-30  10:59
 * 解析器工厂
 *
 */
public class TestElementParserFactory {

    /**
     * 获取默认解析器
     * @return
     */
    public static TestElementParser getDefaultParser() {
        return new JmeterTestElementParser();
    }
}
