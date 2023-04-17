package io.metersphere.plugin.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.plugin.util.PluginLogUtils;
import lombok.Data;

import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "clazzName")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TestElementDTO {
    // 组件类型
    private String type;

    // 用于数据反射对象
    private String clazzName = TestElementDTO.class.getCanonicalName();

    // 当前组件唯一标示
    private String resourceId;

    // 组件标签名称
    private String name;

    // 是否展开收起操作
    private boolean active;

    // 组件索引
    private String index;

    // 是否禁用/启用标示
    private boolean enable = true;

    // 子组件
    private LinkedList<TestElementDTO> hashTree;

    // 父类
    private TestElementDTO parent;

    /**
     * 公共环境逐层传递，如果自身有环境 以自身引用环境为准否则以公共环境作为请求环境
     */
    public void toHashTree(HashTree tree, List<TestElementDTO> hashTree, BaseConfigDTO config) {
        if (hashTree != null && !hashTree.isEmpty()) {
            for (TestElementDTO el : hashTree) {
                el.toHashTree(tree, el.hashTree, config);
            }
        }
    }

    /**
     * 转换JMX
     *
     * @param hashTree
     * @return
     */
    public String getJmx(HashTree hashTree) {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
            SaveService.saveTree(hashTree, bas);
            return bas.toString();
        } catch (Exception e) {
            PluginLogUtils.error("HashTree error, can't log jmx scenarioDefinition");
        }
        return null;
    }

    public HashTree generateHashTree(BaseConfigDTO config) {
        HashTree jmeterTestPlanHashTree = new ListedHashTree();
        this.toHashTree(jmeterTestPlanHashTree, this.hashTree, config);
        return jmeterTestPlanHashTree;
    }
}





