package io.metersphere.system.uid;

import io.metersphere.system.uid.impl.CachedUidGenerator;
import io.metersphere.sdk.util.CommonBeanFactory;

public class UUID {
    private static final CachedUidGenerator DEFAULT_UID_GENERATOR;

    static {
        DEFAULT_UID_GENERATOR = CommonBeanFactory.getBean(CachedUidGenerator.class);
    }

    /**
     * 生成一个唯一的数字
     */
    public static Long randomUUID() {
        return DEFAULT_UID_GENERATOR.getUID();
    }

    /**
     * 生成一个唯一的字符串
     */
    public static String nextStr() {
        return String.valueOf(DEFAULT_UID_GENERATOR.getUID());
    }
}
