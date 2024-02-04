package io.metersphere.system.uid;

import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.uid.impl.DefaultUidGenerator;

public class IDGenerator {
    private static final DefaultUidGenerator DEFAULT_UID_GENERATOR;

    static {
        DEFAULT_UID_GENERATOR = CommonBeanFactory.getBean(DefaultUidGenerator.class);
    }

    /**
     * 生成一个唯一的数字
     */
    public static Long nextNum() {
        return DEFAULT_UID_GENERATOR.getUID();
    }

    /**
     * 生成一个唯一的字符串
     */
    public static String nextStr() {
        return String.valueOf(DEFAULT_UID_GENERATOR.getUID());
    }
}
