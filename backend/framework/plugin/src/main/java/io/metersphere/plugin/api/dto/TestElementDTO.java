package io.metersphere.plugin.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.plugin.util.PluginLogUtils;
import lombok.Data;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TestElementDTO implements Serializable {
    // 组件类型
    private String type;

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
    private LinkedList<TestElementDTO> hashTree;

    // 父类
    private TestElementDTO parent;

    /**
     * 自组件重新这个方法
     *
     * @param tree
     * @param hashTree
     * @param config
     */
    public void toHashTree(ListedHashTree tree, List<TestElementDTO> hashTree, BaseConfigDTO config) {
        if (hashTree != null && hashTree.size() > 0) {
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
    public String getJmx(ListedHashTree hashTree) {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
            SaveService.saveTree(hashTree, bas);
            return bas.toString();
        } catch (Exception e) {
            PluginLogUtils.error("ListedHashTree error, can't log jmx scenarioDefinition");
        }
        return null;
    }

    /**
     * 生成hashTree
     *
     * @param config
     * @return
     */
    public ListedHashTree generateHashTree(BaseConfigDTO config) {
        ListedHashTree listedHashTree = new ListedHashTree();
        this.toHashTree(listedHashTree, this.hashTree, config);
        return listedHashTree;
    }

}





