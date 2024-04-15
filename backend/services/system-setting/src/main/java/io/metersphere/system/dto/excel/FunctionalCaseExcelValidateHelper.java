package io.metersphere.system.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Set;

@Component
public class FunctionalCaseExcelValidateHelper {

    private static FunctionalCaseExcelValidateHelper excelValidateHelper;

    @Resource
    Validator validator;

    public static <T> String validateEntity(T obj) throws NoSuchFieldException {
        StringBuilder result = new StringBuilder();
        Set<ConstraintViolation<T>> set = excelValidateHelper.validator.validate(obj, Default.class);
        if (set != null && !set.isEmpty()) {
            for (ConstraintViolation<T> cv : set) {
                Field declaredField = obj.getClass().getDeclaredField(cv.getPropertyPath().toString());
                ExcelProperty annotation = declaredField.getAnnotation(ExcelProperty.class);
                //拼接错误信息，包含当前出错数据的标题名字+错误信息
                result.append("[").append(annotation.value()[0]).append("]").append(cv.getMessage()).append("; ");
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