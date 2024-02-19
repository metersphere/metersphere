package io.metersphere.system.base.param;

import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.RandomStringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author jianxing
 */
public class SizeParamGenerator extends ParamGenerator {

    /**
     * 生成超过指定长度的字符串
     */
    @Override
    public Object invalidGenerate(Annotation annotation, Field field) {
        Size sizeAnnotation = (Size) annotation;
        int max = sizeAnnotation.max();
        if (isNumberType(field.getType())) {
            return max + 1;
        } else {
            // todo 做缓存优化
            return RandomStringUtils.randomAlphanumeric(max + 1);
        }
    }
}
