package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.api.processor.ScriptProcessor;
import org.apache.jmeter.testelement.TestElement;


/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public abstract class SqlProcessorConverter extends MsProcessorConverter<SQLProcessor> {

    protected void parse(TestElement testElement, ScriptProcessor scriptProcessor) {
        // todo 等环境开发完之后，补充
    }
}
