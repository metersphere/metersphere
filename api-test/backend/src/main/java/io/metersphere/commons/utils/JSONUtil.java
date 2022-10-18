package io.metersphere.commons.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.exception.MSException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class JSONUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 自动检测所有类的全部属性
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public static <T> T parseObject(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJSONString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject createJsonObject(boolean isOrderliness) {
        JSONObject returnObj = new JSONObject();
        if (isOrderliness) {
            try {
                Class jsonObjectClass = returnObj.getClass();//获取Class对象
                Field field = jsonObjectClass.getDeclaredField("map");
                field.setAccessible(true);
                field.set(returnObj, new LinkedHashMap<>());
                field.setAccessible(false);
            } catch (Exception e) {
                LogUtil.error("生成有序JSONObject失败！", e);
            }
        }
        return returnObj;
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
            if (jsonObject != null) {
                //获取clazz的实例
                obj = clazz.getDeclaredConstructor().newInstance();
                // 获取属性列表
                Field[] fields = clazz.getDeclaredFields();
                // 遍历每个属性，如果为基本类型和String则直接赋值，如果为List则得到每个Item添加后再赋值，如果是其它类则得到类的实例后赋值
                for (Field field : fields) {
                    try {
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
                                typeObj = ArrayList.class.getDeclaredConstructor().newInstance();
                            } else if (StringUtils.containsIgnoreCase(typeClazz.getSimpleName(), PropertyConstant.INTEGER)) {
                                typeObj = Integer.class.getConstructor(int.class).newInstance(10);
                            } else if (StringUtils.containsIgnoreCase(typeClazz.getSimpleName(), "Map")) {
                                typeObj = LinkedHashMap.class.getConstructor().newInstance();
                            } else if (StringUtils.containsIgnoreCase(typeClazz.getSimpleName(), PropertyConstant.BOOLEAN)) {
                                typeObj = Boolean.class.getConstructor(boolean.class).newInstance(false);
                            } else {
                                typeObj = typeClazz.getDeclaredConstructor().newInstance();
                            }
                            // 是否为List
                            if (typeObj instanceof List) {
                                // 得到类型的结构，如:java.util.ArrayList<com.xxx.xxx>
                                Type type = field.getGenericType();
                                ParameterizedType pt = (ParameterizedType) type;
                                // 获得List元素类型
                                Class<?> dataClass = (Class<?>) pt.getActualTypeArguments()[0];
                                // 得到List的JSONArray数组
                                JSONArray jArray = jsonObject.optJSONArray(field.getName());
                                if (jArray == null) {
                                    continue;
                                }
                                // 将每个元素的实例类加入到类型的实例中
                                for (int i = 0; i < jArray.length(); i++) {
                                    //对于数组，递归调用解析子元素
                                    ((List<Object>) typeObj).add(parseObject(dataClass, jsonObject.optJSONArray(field.getName()).optJSONObject(i)));
                                }
                                setProperty(obj, field, typeObj);
                            } else if (typeObj instanceof String) {// 是否为String
                                setProperty(obj, field, jsonObject.opt(field.getName()));
                            } else {
                                //是否为其它对象
                                setProperty(obj, field, parseObject(typeClazz, jsonObject.optJSONObject(field.getName())));
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.error(e);
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
        if (ObjectUtils.isEmpty(valueObj) || StringUtils.isEmpty(field.getName())
                || StringUtils.equalsIgnoreCase(field.getName(), "NULL")
                || StringUtils.equalsIgnoreCase(JSONObject.valueToString(valueObj), "NULL")) {
            return;
        }
        Class<?> clazz = obj.getClass();
        try {
            //获取类的setXxx方法，xxx为属性
            String startStr = "set";
            if (StringUtils.equalsIgnoreCase(field.getType().getName(), "boolean") && field.getName().startsWith("is")) {
                startStr += field.getName().substring(2);
            } else {
                startStr += field.getName().substring(0, 1).toUpperCase(Locale.getDefault()) + field.getName().substring(1);
            }
            Method method = clazz.getDeclaredMethod(startStr, field.getType());
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

    public static ArrayNode parseArrayNode(String text) {
        try {
            return (ArrayNode) objectMapper.readTree(text);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return objectMapper.createArrayNode();
    }

    public static ObjectNode parseObjectNode(String text) {
        try {
            return (ObjectNode) objectMapper.readTree(text);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return objectMapper.createObjectNode();
    }

    public static ObjectNode createObj() {
        return objectMapper.createObjectNode();
    }

    public static ArrayNode createArray() {
        return objectMapper.createArrayNode();
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

