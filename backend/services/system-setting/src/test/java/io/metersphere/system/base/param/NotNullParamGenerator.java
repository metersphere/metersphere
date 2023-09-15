package io.metersphere.system.base.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author jianxing
 */
public class NotNullParamGenerator extends ParamGenerator {

    /**
     * 返回 null
     */
    @Override
    public Object invalidGenerate(Annotation annotation, Field field) {
        return null;
    }
}
