package io.metersphere.plugin.api.spi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.LinkedList;

/**
*@Author: jianxing
*@CreateTime: 2023-10-30  15:08
*/
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "polymorphicName")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractMsTestElement implements MsTestElement {

    // 组件标签名称
    private String name;
    // 是否启用
    private Boolean enable = true;

    // 子组件
    private LinkedList<AbstractMsTestElement> children;
}
