package io.metersphere.track.issue;

import io.metersphere.commons.utils.EncryptUtils;

public class ZentaoUtils {

    /**
     * @param code Zentao 应用代号
     * @param key Zentao 密钥
     * @return token
     */
    public static String getToken(String code, String key, String time) {
        return (String) EncryptUtils.md5Encrypt(code + key + time);
    }

    /**
     * @param url Zentao url
     * @param code Zentao 应用代号
     * @param key Zentao 密钥
     * @return url
     */
    public static String getUrl(String url, String code, String key) {
        String time = String.valueOf(System.currentTimeMillis());;
        return url + "api.php?" + "code=" + code + "&time=" + time + "&token=" + getToken(code, key, time);
    }
}
