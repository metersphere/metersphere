package io.metersphere.api.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class ApiDefinitionUtils {

    public static boolean isUrlInList(String apiUrl, Collection<String> customRequestUrlList) {
        if (CollectionUtils.isEmpty(customRequestUrlList)) {
            return false;
        }

        String urlSuffix = apiUrl.trim();
        if (urlSuffix.startsWith("/")) {
            urlSuffix = urlSuffix.substring(1);
        }
        String[] urlParams = urlSuffix.split("/");

        for (String customRequestUrl : customRequestUrlList) {
            if (StringUtils.equalsAny(customRequestUrl, apiUrl, "/" + apiUrl)) {
                return true;
            } else {
                if (StringUtils.isEmpty(customRequestUrl)) {
                    continue;
                }

                customRequestUrl = pretreatmentUrl(customRequestUrl);

                String[] customUrlArr = customRequestUrl.split("/");

                if (StringUtils.startsWithAny(customRequestUrl.toLowerCase(), "https://", "http://")
                        && customUrlArr.length >= urlParams.length) {
                    boolean isFetch = true;
                    for (int urlIndex = 0; urlIndex < urlParams.length; urlIndex++) {
                        String urlItem = urlParams[urlIndex];
                        String customUrlItem = customUrlArr[customUrlArr.length - urlParams.length + urlIndex];
                        // 不为rest参数的要进行全匹配。 而且忽略大小写
                        if (isNotRestUrlParam(customUrlItem) && isNotRestUrlParam(urlItem)) {
                            if (!StringUtils.equalsIgnoreCase(customUrlItem, urlItem)) {
                                isFetch = false;
                                break;
                            }
                        }
                    }
                    if (isFetch) {
                        return true;
                    }
                } else if (customUrlArr.length == urlParams.length) {
                    boolean isFetch = true;
                    for (int urlIndex = 0; urlIndex < urlParams.length; urlIndex++) {
                        String urlItem = urlParams[urlIndex];
                        String customUrlItem = customUrlArr[urlIndex];
                        // 不为rest参数的要进行全匹配。 而且忽略大小写
                        if (isNotRestUrlParam(customUrlItem) && isNotRestUrlParam(urlItem)) {
                            if (!StringUtils.equalsIgnoreCase(customUrlItem, urlItem)) {
                                isFetch = false;
                                break;
                            }
                        }
                    }
                    if (isFetch) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String pretreatmentUrl(String customRequestUrl) {
        if (customRequestUrl.startsWith("/")) {
            customRequestUrl = customRequestUrl.substring(1);
        }

        if (customRequestUrl.contains("?")) {
            customRequestUrl = customRequestUrl.substring(0, customRequestUrl.indexOf("?"));
        }
        return customRequestUrl;
    }

    private static boolean isNotRestUrlParam(String urlParam) {
        return !StringUtils.startsWith(urlParam, "{") || !StringUtils.endsWith(urlParam, "}") || StringUtils.equals(urlParam, "{}");
    }
}
