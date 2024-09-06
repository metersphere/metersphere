package io.metersphere.sdk.valid;

import io.metersphere.sdk.constants.ValueEnum;
import io.metersphere.sdk.util.Translator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.collections.CollectionUtils;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 枚举值校验，即值只能是指定枚举类中的值
 * @author jianxing
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValue.EnumValueValidator.class)
public @interface EnumValue {
    /**
     * 错误提示
     */
    String message() default "{enum_value_valid_message}";

    /**
     * 必须的属性
     * 用于分组校验
     */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 枚举类
     */
    Class<? extends Enum> enumClass();

    /**
     * 校验时排除枚举中的某些值，只支持 String 类型
     */
    String[] excludeValues() default {};

    class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

        private Class<? extends Enum> enumClass;
        private String[] excludeValues;

        @Override
        public void initialize(EnumValue enumValue) {
            this.enumClass = enumValue.enumClass();
            this.excludeValues = enumValue.excludeValues();
        }

        /**
         * 校验参数是否在枚举值中
         * @param value 待校验的参数
         * @param context 上下文
         * @return 校验结果
         */
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            // 获取枚举的所有实例
            Enum<? extends Enum>[] enums = enumClass.getEnumConstants();
            // 获取所有的枚举值
            List<Object> values = new ArrayList<>();
            for (Enum<? extends Enum> item : enums) {
                if (item instanceof ValueEnum) {
                    values.add(((ValueEnum) item).getValue());
                } else {
                    values.add(item.name());
                }
            }
            // 如果设置了排除值，则判断是否在排除值中
            boolean isExcludeValue = excludeValues == null ? false : Arrays.stream(excludeValues).anyMatch(value::equals);
            boolean valid = values.contains(value) && !isExcludeValue;
            if (!valid) {
                // 禁用默认的message的值
                context.disableDefaultConstraintViolation();
                // values 与 excludeValues 的差集
                String errorValues = CollectionUtils.subtract(values, Arrays.asList(excludeValues)).toString();
                // 重新添加错误提示语句
                context.buildConstraintViolationWithTemplate(Translator.get("enum_value_valid_message") + errorValues).addConstraintViolation();
            }

            return valid;
        }
    }
}