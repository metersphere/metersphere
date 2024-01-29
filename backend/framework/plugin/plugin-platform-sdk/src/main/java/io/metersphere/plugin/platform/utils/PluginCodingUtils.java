package io.metersphere.plugin.platform.utils;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * 加密解密工具
 *
 * @author kun.mo
 */
public class PluginCodingUtils {

    private static final String UTF_8 = StandardCharsets.UTF_8.name();
    /**
     * BASE64加密
     *
     * @param src 待加密的字符串
     * @return 加密后的字符串
     */
    public static String base64Encoding(String src) {
        String result = null;
        if (src != null) {
            try {
                result = Base64.encodeBase64String(src.getBytes(UTF_8));
            } catch (Exception e) {
                throw new RuntimeException("BASE64 encoding error:", e);
            }
        }
        return result;
    }
}
