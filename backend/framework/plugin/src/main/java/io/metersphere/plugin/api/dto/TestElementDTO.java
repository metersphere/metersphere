package io.metersphere.plugin.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;

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
}





