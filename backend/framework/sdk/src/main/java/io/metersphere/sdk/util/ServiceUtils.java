package io.metersphere.sdk.util;

import io.metersphere.sdk.exception.MSException;

import static io.metersphere.sdk.controller.handler.result.MsHttpResultCode.NOT_FOUND;

public class ServiceUtils {

    /**
     * 保存资源名称，在处理 NOT_FOUND 异常时，拼接资源名称
     */
    private static final ThreadLocal<String> resourceName = new ThreadLocal<>();

    /**
     * 校验资源是否存在，不存在则抛出 NOT_FOUND 异常
     * @param resource 资源
     * @param name 资源名称，用户拼接异常信息
     * @return
     * @param <T>
     */
    public static <T> T checkResourceExist(T resource, String name) {
        if (resource == null) {
            resourceName.set(name);
            throw new MSException(NOT_FOUND);
        }
        return resource;
    }

    public static String getResourceName() {
        return resourceName.get();
    }

    public static void clearResourceName() {
        resourceName.remove();
    }
}
