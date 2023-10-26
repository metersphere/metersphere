package io.metersphere.plugin.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.plugin.sdk.util.PluginLogUtils;
import lombok.Data;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "polymorphicName")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TestElementDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 当前组件唯一标示
    private String uuid;

    // 组件标签名称
    private String name;

    // 是否展开收起操作
    private boolean active;

    // 组件索引
    private String index;

    // 是否禁用/启用标示
    private boolean enable;

    // 子组件
    private LinkedList<TestElementDTO> children;

    /**
     * 自组件重新这个方法
     */
    public void toHashTree(HashTree tree, List<TestElementDTO> children, BaseConfigDTO config) {
        if (children != null && !children.isEmpty()) {
            for (TestElementDTO el : children) {
                el.toHashTree(tree, el.children, config);
            }
        }
    }

    /**
     * 转换JMX
     */
    public String getJmx(HashTree children) {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
            SaveService.saveTree(children, bas);
            return bas.toString();
        } catch (Exception e) {
            PluginLogUtils.error("HashTree error, can't log jmx scenarioDefinition");
        }
        return null;
    }

    /**
     * 生成hashTree
     */
    public HashTree generateHashTree(BaseConfigDTO config) {
        HashTree listedHashTree = new HashTree();
        this.toHashTree(listedHashTree, this.children, config);
        return listedHashTree;
    }

}





