package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.plugin.core.utils.LogUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
@Data
public class BaseEnvElement {
    // 组件类型
    private String type;

    // 用于数据反射对象
    private String clazzName = MsTestElement.class.getCanonicalName();

    // 自身资源ID（用例ID/接口ID/场景ID)等
    private String id;

    // 当前组件唯一标示
    private String resourceId;

    // 组件标签名称
    private String name;

    // 组件标签
    private String label;

    // 引用对象标示
    private String referenced;

    // 是否展开收起操作
    private boolean active;
    // 组件索引
    private String index;

    // 是否禁用/启用标示
    private boolean enable = true;

    // 引用对象类型（REF，Created,DELETE）
    private String refType;

    // 子组件
    private LinkedList<MsTestElement> hashTree;

    // 项目ID
    private String projectId;

    // 是否是mock环境
    private boolean isMockEnvironment;

    // 自身环境Id
    private String environmentId;

    // 插件ID
    private String pluginId;

    // 步骤别名
    private String stepName;
}
