package io.metersphere.sdk.util;

import java.util.List;
import java.util.stream.Collectors;

public class EncryptUtils extends CodingUtils {

    private static final String secretKey = "www.fit2cloud.co";
    private static final String iv = "1234567890123456";


    public static String aesEncrypt(Object o) {
        if (o == null) {
            return null;
        }
        return aesEncrypt(o.toString(), secretKey, iv);
    }

    public static String aesDecrypt(Object o) {
        if (o == null) {
            return null;
        }
        return aesDecrypt(o.toString(), secretKey, iv);
    }

    public static <T> Object aesDecrypt(List<T> o, String attrName) {
        if (o == null) {
            return null;
        }
        return o.stream()
                .filter(element -> BeanUtils.getFieldValueByName(attrName, element) != null)
                .peek(element -> BeanUtils.setFieldValueByName(element, attrName, aesDecrypt(BeanUtils.getFieldValueByName(attrName, element).toString(), secretKey, iv), String.class))
                .collect(Collectors.toList());
    }

    public static String md5Encrypt(Object o) {
        if (o == null) {
            return null;
        }
        return md5(o.toString());
    }
}
