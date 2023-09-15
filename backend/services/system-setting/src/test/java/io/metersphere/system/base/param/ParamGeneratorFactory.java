package io.metersphere.system.base.param;

import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jianxing
 */
public class ParamGeneratorFactory {

    private static final Map<String, ParamGenerator> paramGeneratorMap = new HashMap<>() {{
        put(Size.class.getName(), new SizeParamGenerator());
        put(NotBlank.class.getName(), new NotBlankParamGenerator());
        put(NotEmpty.class.getName(), new NotEmptyParamGenerator());
        put(NotNull.class.getName(), new NotNullParamGenerator());
        put(EnumValue.class.getName(), new EnumValueParamGenerator());
        put(Min.class.getName(), new MinParamGenerator());
        put(Max.class.getName(), new MaxParamGenerator());
        put(Email.class.getName(), new EmailParamGenerator());
    }};

    public static ParamGenerator getParamGenerator(Annotation annotation) {
        return paramGeneratorMap.get(annotation.annotationType().getName());
    }

    /**
     * 根据指定的参数定义和分组，自动生成异常参数列表
     * @param group 接口分组
     * @param clazz 参数类型
     * @return 异常参数列表
     * @param <T> 参数类型
     * @throws Exception
     */
    public static <T> List<InvalidateParamInfo> generateInvalidateParams(Class group, Class<T> clazz) throws Exception {
        List<InvalidateParamInfo> paramList = new ArrayList();
        // 通过反射获取当前类的所有属性
        Field[] fields = clazz.getDeclaredFields();
        // 遍历属性，获取每个属性上的所有注解
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            // 遍历注解，获取注解的类型
            for (Annotation annotation : annotations) {
                // 获取处理指定注解的生成器
                ParamGenerator paramGenerator = getParamGenerator(annotation);
                // 生成器不为空，并且如果有指定 group 时，需要匹配 group
                if (paramGenerator != null && paramGenerator.isGroupMatch(group, annotation)) {
                    T param = clazz.getConstructor().newInstance();
                    // 生成异常数据
                    Object invalidValue = paramGenerator.invalidGenerate(annotation, field);
                    // 将参数值赋为异常数据
                    getSetMethod(clazz, field).invoke(param, invalidValue);
                    InvalidateParamInfo invalidateParamInfo =
                            new InvalidateParamInfo(field.getName(), param, annotation.annotationType());
                    paramList.add(invalidateParamInfo);
                }
                // 如果是嵌套的校验注解，需要递归处理
                if (StringUtils.equalsAny(annotation.annotationType().getName(), Valid.class.getName(), Validated.class.getName())) {
                    Class<?> fieldClazz = Class.forName(field.getType().getName());
                    if (List.class.isAssignableFrom(fieldClazz)) {
                        // 如果是数组，解析数组里的泛型
                        Class genericClazz = getaGenericClass(field, 0);
                        List<InvalidateParamInfo> genericParamList = generateInvalidateParams(group, genericClazz);
                        for (InvalidateParamInfo genericParam : genericParamList) {
                            List list = new ArrayList(1);
                            list.add(genericParam.getValue());
                            T param = clazz.getConstructor().newInstance();
                            getSetMethod(clazz, field).invoke(param, list);
                            InvalidateParamInfo invalidateParamInfo = new InvalidateParamInfo(field.getName() + "[0]" + "." + genericParam.getName(),
                                    param, genericParam.getAnnotation());
                            paramList.add(invalidateParamInfo);
                        }
                    } else if (Map.class.isAssignableFrom(fieldClazz)) {
                        // 如果是 Map，解析数组里的泛型
                        Class genericClazz = getaGenericClass(field, 1);
                        List<InvalidateParamInfo> genericParamList = generateInvalidateParams(group, genericClazz);
                        for (InvalidateParamInfo genericParam : genericParamList) {
                            Map map = new HashMap(1);
                            map.put("key", genericParam.getValue());
                            T param = clazz.getConstructor().newInstance();
                            getSetMethod(clazz, field).invoke(param, map);
                            InvalidateParamInfo invalidateParamInfo = new InvalidateParamInfo(field.getName() + "[key]" + "." + genericParam.getName(),
                                    param, genericParam.getAnnotation());
                            paramList.add(invalidateParamInfo);
                        }
                    } else {
                        // 如果是对象，解析对象里的泛型
                        List<InvalidateParamInfo> objParamList = generateInvalidateParams(group, field.getType());
                        for (InvalidateParamInfo objParam : objParamList) {
                            T newParam = clazz.getConstructor().newInstance();
                            BeanUtils.copyBean(newParam, newParam);
                            getSetMethod(clazz, field).invoke(newParam, objParam.getValue());
                            InvalidateParamInfo invalidateParamInfo = new InvalidateParamInfo(field.getName() + "." + objParam.getName(),
                                    newParam, objParam.getAnnotation());
                            paramList.add(invalidateParamInfo);
                        }
                    }
                }
            }
        }
        return paramList;
    }

    private static Class getaGenericClass(Field field, int index) throws ClassNotFoundException {
        Class genericClazz = Class.forName(
                (
                        (Class) ((ParameterizedType) field.getGenericType())
                                .getActualTypeArguments()[index]
                ).getName()
        );
        return genericClazz;
    }

    private static Method getSetMethod(Class clazz, Field field) throws Exception {
        return clazz.getMethod("set" + StringUtils.capitalize(field.getName()), field.getType());
    }
}
