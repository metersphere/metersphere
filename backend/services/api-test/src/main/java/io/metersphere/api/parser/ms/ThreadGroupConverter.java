package io.metersphere.api.parser.ms;


import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class ThreadGroupConverter extends AbstractMsElementConverter<ThreadGroup> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, ThreadGroup element, HashTree hashTree) {
        MsScenario msScenario = new MsScenario();
        // todo 解析线程组
        parent.getChildren().add(msScenario);
        parseChild(msScenario, element, hashTree);
    }
}
