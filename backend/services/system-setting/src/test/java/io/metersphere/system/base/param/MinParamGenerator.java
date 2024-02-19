package io.metersphere.system.base.param;

import jakarta.validation.constraints.Min;
import org.apache.commons.lang3.RandomStringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author jianxing
 */
public class MinParamGenerator extends ParamGenerator {

    /**
     * 返回 null
     */
    @Override
    public Object invalidGenerate(Annotation annotation, Field field) {
        Min minAnnotation = (Min) annotation;
        if (isNumberType(field.getType())) {
            return convertToNumberType(field.getType(), minAnnotation.value() - 1);
        } else {
            // todo 做缓存优化
            return RandomStringUtils.random((int) (minAnnotation.value() - 1));
        }
    }
}
