package io.metersphere.plugin.api.spi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * <pre>
 * 该对象传参时，需要传入 polymorphicName 字段，用于区分是哪个协议的组件
 * 对应协议的组件 polymorphicName 字段，调用 /api/test/protocol/{organizationId} 接口获取
 * <pre>
 * @Author: jianxing
 * @CreateTime: 2023-10-30  15:08
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "polymorphicName")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractMsTestElement implements MsTestElement {
    /**
     * 步骤ID（唯一）
     * 运行时设置
     */
    private String stepId;
    /**
     * 关联的资源ID（用例ID/接口ID/场景ID)等
     * 运行时设置
     */
    private String resourceId;
    /**
     * 当前的项目ID
     * 运行时设置
     */
    private String projectId;
    /**
     * 组件标签名称
     */
    private String name;
    /**
     * 是否启用
     */
    private Boolean enable = true;
    /**
     * 子组件
     */
    private LinkedList<AbstractMsTestElement> children = new LinkedList<>();
    /**
     * 父组件
     */
    private AbstractMsTestElement parent;
    /**
     * 关联的 csv ID
     */
    private List<String> csvIds;
}
