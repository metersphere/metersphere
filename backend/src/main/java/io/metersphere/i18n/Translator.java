package io.metersphere.i18n;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import io.metersphere.commons.constants.I18nConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.SystemParameterService;
import io.metersphere.user.SessionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Translator {

    public static final String PREFIX = "$[{";
    public static final String SUFFIX = "}]";
    private static final String JSON_SYMBOL = "\":";

    private static final HashSet<String> IGNORE_KEYS = new HashSet<>(Arrays.asList("id", "password", "passwd"));

    private static Map<String, String> langCache4Thread = Collections.synchronizedMap(new PassiveExpiringMap(1, TimeUnit.MINUTES));

    public static String getLangDes() {
        return getLang().getDesc();
    }

    public static Lang getLang() {
        HttpServletRequest request = getRequest();
        return getLang(request);
    }

    public static Object gets(Object keys) {
        return gets(getLang(), keys);
    }

    public static Object gets(Lang lang, Object keys) {
        Map<String, String> context = I18nManager.getI18nMap().get(lang.getDesc().toLowerCase());
        return translateObject(keys, context);
    }

    // 单Key翻译
    public static String get(String key) {
        return get(getLang(), key);
    }

    public static String get(Lang lang, String key) {
        if (StringUtils.isBlank(key)) {
            return StringUtils.EMPTY;
        }
        return translateKey(key, I18nManager.getI18nMap().get(lang.getDesc().toLowerCase()));
    }

    public static String toI18nKey(String key) {
        return String.format("%s%s%s", PREFIX, key, SUFFIX);
    }

    private static HttpServletRequest getRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    private static Lang getLang(HttpServletRequest request) {
        String preferLang = Lang.zh_CN.getDesc();

        try {
            if (request != null) {
                Object sessionLang = request.getSession(true).getAttribute(I18nConstants.LANG_COOKIE_NAME);
                if (sessionLang != null && StringUtils.isNotBlank(sessionLang.toString())) {
                    return Lang.getLang(sessionLang.toString());
                }
                preferLang = getSystemParameterLanguage(preferLang);
                if (StringUtils.isNotBlank(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE))) {
                    String preferLangWithComma = StringUtils.substringBefore(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE), ";");
                    String acceptLanguage = StringUtils.replace(StringUtils.substringBefore(preferLangWithComma, ","), "-", "_");
                    if (Lang.getLangWithoutDefault(acceptLanguage) != null) {
                        preferLang = acceptLanguage;
                    }
                }
                if (request.getCookies() != null && request.getCookies().length > 0) {
                    for (Cookie cookie : request.getCookies()) {
                        if (StringUtils.equalsIgnoreCase(cookie.getName(), I18nConstants.LANG_COOKIE_NAME)) {
                            preferLang = cookie.getValue();
                        }
                    }
                }
                if (SessionUtils.getUser() != null && StringUtils.isNotBlank(SessionUtils.getUser().getLanguage())) {
                    preferLang = SessionUtils.getUser().getLanguage();
                }
                request.getSession(true).setAttribute(I18nConstants.LANG_COOKIE_NAME, preferLang);
            } else {
                preferLang = getSystemParameterLanguage(preferLang);
            }
        } catch (Exception e) {
            LogUtil.error("Fail to getLang.", e);
        }

        return Lang.getLang(preferLang);
    }

    private static String getSystemParameterLanguage(String defaultLang) {
        String result = defaultLang;
        try {
            String cachedLang = langCache4Thread.get(I18nConstants.LANG_COOKIE_NAME);
            if (StringUtils.isNotBlank(cachedLang)) {
                return cachedLang;
            }
            String systemLanguage = Objects.requireNonNull(CommonBeanFactory.getBean(SystemParameterService.class)).getSystemLanguage();
            if (StringUtils.isNotBlank(systemLanguage)) {
                result = systemLanguage;
            }
            langCache4Thread.put(I18nConstants.LANG_COOKIE_NAME, result);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return result;

    }

    private static Object translateObject(Object javaObject, final Map<String, String> context) {
        if (MapUtils.isEmpty(context)) {
            return javaObject;
        }
        if (javaObject == null) {
            return null;
        }

        try {
            if (javaObject instanceof String) {
                String rawString = javaObject.toString();
                if (StringUtils.contains(rawString, JSON_SYMBOL)) {
                    try {
                        Object jsonObject = JSON.parse(rawString);
                        Object a = translateObject(jsonObject, context);
                        return JSON.toJSONString(a);
                    } catch (Exception e) {
                        LogUtil.warn("Failed to translate object " + rawString + ". Error: " + ExceptionUtils.getStackTrace(e));
                        return translateRawString(null, rawString, context);
                    }

                } else {
                    return translateRawString(null, rawString, context);
                }
            }

            if (javaObject instanceof Map) {
                Map<Object, Object> map = (Map<Object, Object>) javaObject;

                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    if (entry.getValue() != null) {
                        if (entry.getValue() instanceof String) {
                            if (StringUtils.contains(entry.getValue().toString(), JSON_SYMBOL)) {
                                map.put(entry.getKey(), translateObject(entry.getValue(), context));
                            } else {
                                map.put(entry.getKey(), translateRawString(entry.getKey().toString(), entry.getValue().toString(), context));
                            }
                        } else {
                            translateObject(entry.getValue(), context);
                        }
                    }
                }

            }

            if (javaObject instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) javaObject;
                for (Object item : collection) {
                    translateObject(item, context);
                }
            }

            if (javaObject.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(javaObject); ++i) {
                    Object item = Array.get(javaObject, i);
                    Array.set(javaObject, i, translateObject(item, context));
                }
            }

            ObjectSerializer serializer = SerializeConfig.globalInstance.getObjectWriter(javaObject.getClass());
            if (serializer instanceof JavaBeanSerializer) {
                JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) serializer;

                try {
                    Map<String, Object> values = javaBeanSerializer.getFieldValuesMap(javaObject);
                    for (Map.Entry<String, Object> entry : values.entrySet()) {
                        if (entry.getValue() != null) {
                            if (entry.getValue() instanceof String) {
                                if (StringUtils.contains(entry.getValue().toString(), JSON_SYMBOL)) {
                                    BeanUtils.setFieldValueByName(javaObject, entry.getKey(), translateObject(entry.getValue(), context), String.class);
                                } else {
                                    BeanUtils.setFieldValueByName(javaObject, entry.getKey(), translateRawString(entry.getKey(), entry.getValue().toString(), context), String.class);
                                }
                            } else {
                                translateObject(entry.getValue(), context);
                            }
                        }
                    }
                } catch (Exception e) {
                    MSException.throwException(e);
                }
            }

            return javaObject;
        } catch (StackOverflowError stackOverflowError) {
            try {
                return JSON.parseObject(translateRawString(null, JSON.toJSONString(javaObject), context).toString(), javaObject.getClass());
            } catch (Exception e) {
                LogUtil.error("Failed to translate object " + javaObject.toString(), e);
                return javaObject;
            }
        }
    }

    private static Object translateRawString(String key, String rawString, Map<String, String> context) {
        if (StringUtils.isBlank(rawString)) {
            return rawString;
        }
        for (String ignoreKey : IGNORE_KEYS) {
            if (StringUtils.containsIgnoreCase(key, ignoreKey)) {
                return rawString;
            }
        }
        if (StringUtils.contains(rawString, PREFIX)) {
            rawString = new StringSubstitutor(context, PREFIX, SUFFIX).replace(rawString);
            if (StringUtils.contains(rawString, PREFIX)) {
                String[] unTrans = StringUtils.substringsBetween(rawString, PREFIX, SUFFIX);
                if (unTrans != null) {
                    for (String unTran : unTrans) {
                        rawString = StringUtils.replace(rawString, PREFIX + unTran + SUFFIX, unTran);
                    }
                }
            }
        }
        if (key != null) {
            String desc = context.get(rawString);
            if (StringUtils.isNotBlank(desc)) {
                return desc;
            }
        }
        return rawString;
    }

    private static String translateKey(String key, Map<String, String> context) {
        if (MapUtils.isEmpty(context)) {
            return key;
        }
        String desc = context.get(StringUtils.replace(StringUtils.replace(key, PREFIX, StringUtils.EMPTY), SUFFIX, StringUtils.EMPTY));
        if (StringUtils.isNotBlank(desc)) {
            return desc;
        }
        return key;
    }
}
