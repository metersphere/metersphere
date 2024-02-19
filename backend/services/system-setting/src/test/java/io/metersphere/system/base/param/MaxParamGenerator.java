package io.metersphere.system.base.param;

import jakarta.validation.constraints.Max;
import org.apache.commons.lang3.RandomStringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author jianxing
 */
public class MaxParamGenerator extends ParamGenerator {

    @Override
    public Object invalidGenerate(Annotation annotation, Field field) {
        Max maxAnnotation = (Max) annotation;
        if (isNumberType(field.getType())) {
            return convertToNumberType(field.getType(), maxAnnotation.value() + 1);
        } else {
            // todo 做缓存优化
            return RandomStringUtils.random((int) (maxAnnotation.value() + 1));
        }
    }
}
