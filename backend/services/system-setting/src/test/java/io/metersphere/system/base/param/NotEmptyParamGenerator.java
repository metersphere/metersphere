package io.metersphere.system.base.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author jianxing
 */
public class NotEmptyParamGenerator extends ParamGenerator {

    /**
     * 生成空字符串
     */
    @Override
    public Object invalidGenerate(Annotation annotation, Field field) {
        if (field.getType().equals(List.class)) {
            return new ArrayList<>(0);
        }
        if (field.getType().equals(Set.class)) {
            return new HashSet<>(0);
        }
        if (field.getType().equals(Map.class)) {
            return new HashMap<>(0);
        }
        throw new RuntimeException("不支持该类型");
    }
}
