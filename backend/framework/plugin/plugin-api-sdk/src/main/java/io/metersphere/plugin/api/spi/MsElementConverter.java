package io.metersphere.plugin.api.spi;


import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.pf4j.ExtensionPoint;

/**
 * @author jianxing
 * @createTime 2021-10-30  10:07
 * 将 HashTree 转换为 MsTestElement
 * 接口导入 jmx 格式时，解析扩展的 HashTree
 * 要支持导入，插件需要做以下修改：
 * 1. 编写 com.thoughtworks.xstream.converters.Converter 的实现类，以实现 xstream 的 xml 反序列化解析
 *    实现类示例：
 *      public class TCPXStreamConverter extends TestElementConverter {
 *         public TCPXStreamConverter(Mapper mapper) {
 *             super(mapper);
 *         }
 *        @Override
 *         public boolean canConvert(Class clazz) {
 *             return TCPSampler.class.isAssignableFrom(clazz);
 *         }
 *      }
 *
 * 2. 在插件的 resource 下新建 META-INF/services/com.thoughtworks.xstream.converters.Converter 文件
 *    并添加实现类的全限定名，已便于SPI的服务发现
 *
 * 3. 在插件的 resource 下新建 jmeter_element_alias.properties 配置文件，用于配置元素的别名
 *    文件内容示例（如果是jmeter官方支持的组件，可以省略这步）：
 *      TCPSampler=org.apache.jmeter.protocol.tcp.sampler.TCPSampler
 *      TCPSamplerGui=org.apache.jmeter.protocol.tcp.control.gui.TCPSamplerGui
 *
 * 4. 编写 MsElementConverter 的实现类，将 HashTree 转换为 MsTestElement
 */
public interface MsElementConverter<T extends TestElement> extends ExtensionPoint {

    /**
     * 将 HashTree 转换为 MsTestElement
     */
    void toMsElement(AbstractMsTestElement parent, T element, HashTree hashTree);
}
