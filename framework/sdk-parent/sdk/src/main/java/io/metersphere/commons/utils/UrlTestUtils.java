package io.metersphere.commons.utils;

import io.metersphere.ldap.service.CustomSSLSocketFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlTestUtils {

    public static boolean testUrlWithTimeOut(String address, int timeOutMillSeconds) {
        try {
            if (200 == testUrl(address, timeOutMillSeconds)) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
        return false;
    }

    public static int testUrl(String address, int timeOutMillSeconds) throws IOException {
        URL urlObj = new URL(address);
        HttpURLConnection oc = (HttpURLConnection) urlObj.openConnection();
        if (oc instanceof HttpsURLConnection) {
            ((HttpsURLConnection) oc).setSSLSocketFactory(new CustomSSLSocketFactory());
            ((HttpsURLConnection) oc).setHostnameVerifier((hostname, session) -> true);
        }
        oc.setUseCaches(false);
        oc.setConnectTimeout(timeOutMillSeconds); // 设置超时时间
        return oc.getResponseCode();// 请求状态
    }
}
