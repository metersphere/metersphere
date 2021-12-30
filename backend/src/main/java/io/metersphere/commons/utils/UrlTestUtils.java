package io.metersphere.commons.utils;

import io.metersphere.ldap.service.CustomSSLSocketFactory;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlTestUtils {

    public static boolean testUrlWithTimeOut(String address, int timeOutMillSeconds) {
        try {
            URL urlObj = new URL(address);
            HttpURLConnection oc = (HttpURLConnection) urlObj.openConnection();
            if (oc instanceof HttpsURLConnection) {
                ((HttpsURLConnection) oc).setSSLSocketFactory(new CustomSSLSocketFactory());
                ((HttpsURLConnection) oc).setHostnameVerifier((hostname, session) -> true);
            }
            oc.setUseCaches(false);
            oc.setConnectTimeout(timeOutMillSeconds); // 设置超时时间
            int status = oc.getResponseCode();// 请求状态
            if (200 == status) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
        return false;
    }
}
