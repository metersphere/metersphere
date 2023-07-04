package io.metersphere.sdk.constants;

/**
 * 用于参数校验注解 EnumValue 的枚举接口
 * 如果枚举定义了类似 value 的值，可以实现改接口，即可使用于 EnumValue 注解
 * 如果枚举值只需要通过 name() 获取，可以不实现该接口
 * @author jianxing
 */
public interface ValueEnum<T> {
    /**
     * 获取枚举值
     * @return  枚举值
     */
    T getValue();
}