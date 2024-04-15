package io.metersphere.system.dto.excel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserExcelValidateHelper {

    private static UserExcelValidateHelper excelValidateHelper;

    @Resource
    Validator validator;

    public static <T> String validateEntity(T obj) throws NoSuchFieldException {
        StringBuilder result = new StringBuilder();
        Set<ConstraintViolation<T>> set = excelValidateHelper.validator.validate(obj, Default.class);
        if (set != null && !set.isEmpty()) {
            for (ConstraintViolation<T> cv : set) {
                result.append(cv.getMessage()).append("; ");
            }
        }
        return result.toString();
    }

    /**
     * 在静态方法中调用
     */
    @PostConstruct
    public void initialize() {
        excelValidateHelper = this;
        excelValidateHelper.validator = this.validator;
    }
}
