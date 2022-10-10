package io.metersphere.code.snippet.util;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JSONUtil {
    /**
     * 接地json字符串到实例对象
     *
     * @param clazz 和JSON对应的类的Class，必须拥有setXxx()函数，其中xxx为属性
     * @param json  被解析的JSON字符串
     * @return 返回传入的Object对象实例
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return parseObject(clazz, jsonObject);
        } catch (Exception e) {
            MSException.throwException("当前数据非JSON格式");
        }
        return null;
    }

    /**
     * 解析JSONObject对象到具体类，递归算法
     *
     * @param clazz      和JSON对应的类的Class，必须拥有setXxx()函数，其中xxx为属性
     * @param jsonObject 被解析的JSON对象
     * @return 返回传入的Object对象实例
     */
    private static <T> T parseObject(Class<T> clazz, JSONObject jsonObject) {
        T obj = null;
        try {
            //获取clazz的实例
            obj = clazz.newInstance();
            // 获取属性列表
            Field[] fields = clazz.getDeclaredFields();
            // 遍历每个属性，如果为基本类型和String则直接赋值，如果为List则得到每个Item添加后再赋值，如果是其它类则得到类的实例后赋值
            for (Field field : fields) {
                // 设置属性可操作
                field.setAccessible(true);
                // 获取字段类型
                Class<?> typeClazz = field.getType();
                // 是否基础变量
                if (typeClazz.isPrimitive()) {
                    setProperty(obj, field, jsonObject.opt(field.getName()));
                } else {
                    // 得到类型实例
                    Object typeObj;
                    if (typeClazz.isInterface() && typeClazz.getSimpleName().contains("List")) {
                        //Field如果声明为List<T>接口，由于接口的Class对象不能newInstance()，此时需要转化为ArrayList
                        typeObj = ArrayList.class.newInstance();
                    } else {
                        typeObj = typeClazz.newInstance();
                    }
                    // 是否为List
                    if (typeObj instanceof List) {
                        // 得到类型的结构，如:java.util.ArrayList<com.xxx.xxx>
                        Type type = field.getGenericType();
                        ParameterizedType pt = (ParameterizedType) type;
                        // 获得List元素类型
                        Class<?> dataClass = (Class<?>) pt.getActualTypeArguments()[0];
                        // 得到List的JSONArray数组
                        JSONArray jArray = jsonObject.getJSONArray(field.getName());
                        // 将每个元素的实例类加入到类型的实例中
                        for (int i = 0; i < jArray.length(); i++) {
                            //对于数组，递归调用解析子元素
                            ((List<Object>) typeObj).add(parseObject(dataClass, jsonObject.getJSONArray(field.getName()).getJSONObject(i)));
                        }
                        setProperty(obj, field, typeObj);
                    } else if (typeObj instanceof String) {// 是否为String
                        setProperty(obj, field, jsonObject.opt(field.getName()));
                    } else {
                        //是否为其它对象
                        setProperty(obj, field, parseObject(typeClazz, jsonObject.getJSONObject(field.getName())));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return obj;
    }

    /**
     * 给实例对象的成员变量赋值
     *
     * @param obj      实例对象
     * @param field    要被赋值的成员变量
     * @param valueObj 要被赋值的成员变量的值
     */
    private static void setProperty(Object obj, Field field, Object valueObj) {
        if (ObjectUtils.isEmpty(valueObj)) {
            return;
        }
        Class<?> clazz = obj.getClass();
        try {
            //获取类的setXxx方法，xxx为属性
            String startStr = "set";
            if (StringUtils.equalsIgnoreCase(field.getType().getName(), "boolean") && field.getName().startsWith("is")) {
                startStr = "";
            }
            Method method = clazz.getDeclaredMethod(startStr + field.getName().substring(0, 1).toUpperCase(Locale.getDefault()) + field.getName().substring(1), field.getType());
            //设置set方法可访问
            method.setAccessible(true);
            //调用set方法为实例对象的成员变量赋值
            if (StringUtils.equalsIgnoreCase(field.getType().getName(), "int")) {
                method.invoke(obj, Integer.parseInt(valueObj.toString()));
            } else if (StringUtils.equalsIgnoreCase(field.getType().getName(), "long")) {
                method.invoke(obj, Long.parseLong(valueObj.toString()));
            } else if (StringUtils.equalsIgnoreCase(field.getType().getName(), "boolean")) {
                method.invoke(obj, Boolean.parseBoolean(valueObj.toString()));
            } else {
                method.invoke(obj, valueObj);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public static JSONObject parseObject(String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                MSException.throwException("value is null");
            }
            Map<String, Object> map = JSON.parseObject(value, Map.class);
            return new JSONObject(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONArray parseArray(String text) {
        List<Object> list = JSON.parseObject(text, List.class);
        return new JSONArray(list);
    }

    public static boolean isEmpty(JSONArray array) {
        return array == null || array.length() == 0;
    }

    public static boolean isNotEmpty(JSONArray array) {
        return array != null && array.length() > 0;
    }

    public static boolean isEmpty(JSONObject object) {
        return object == null || object.length() == 0;
    }

    public static boolean isNotEmpty(JSONObject object) {
        return object != null && object.length() > 0;
    }
}
