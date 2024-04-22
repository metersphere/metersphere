package io.metersphere.system.dto.excel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

@Component
public class UserExcelValidateHelper {

    private static UserExcelValidateHelper excelValidateHelper;

    @Resource
    Validator validator;

    public static <T> String validateEntity(T obj) throws NoSuchFieldException {
        StringBuilder result = new StringBuilder();
        Set<ConstraintViolation<T>> set = excelValidateHelper.validator.validate(obj, Default.class);
        if (set != null && !set.isEmpty()) {
            //报错信息进行有序、去重处理
            Set<String> errorMsgSet = new TreeSet<>();
            for (ConstraintViolation<T> cv : set) {
                errorMsgSet.add(cv.getMessage());
            }
            errorMsgSet.forEach(item -> result.append(item).append("; "));
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
