package base.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author jianxing
 */
public class NotEmptyParamGenerator extends ParamGenerator {

    /**
     * 生成空字符串
     */
    @Override
    public Object invalidGenerate(Annotation annotation, Field field) {
        return new ArrayList<>(0);
    }
}
