package io.metersphere.constants;

import java.util.HashMap;
import java.util.Map;

public enum HttpMethodConstants {
    GET,
    HEAD,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE;

    private static final Map<String, HttpMethodConstants> mappings = new HashMap(16);

    private HttpMethodConstants() {
    }

    public static HttpMethodConstants resolve(String method) {
        return method != null ? (HttpMethodConstants) mappings.get(method) : null;
    }

    public boolean matches(String method) {
        return this.name().equals(method);
    }

    static {
        HttpMethodConstants[] var0 = values();
        int var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2) {
            HttpMethodConstants HttpMethodConstants = var0[var2];
            mappings.put(HttpMethodConstants.name(), HttpMethodConstants);
        }

    }
}
