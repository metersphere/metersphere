package io.metersphere.excel.domain;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class UserExcelDataFactory implements ExcelDataFactory {
    @Override
    public Class getExcelDataByLocal() {
        Locale locale = LocaleContextHolder.getLocale();
        if (Locale.US.toString().equalsIgnoreCase(locale.toString())) {
            return UserExcelDataUs.class;
        } else if (Locale.TRADITIONAL_CHINESE.toString().equalsIgnoreCase(locale.toString())) {
            return UserExcelDataTw.class;
        }
        return UserExcelDataCn.class;
    }
}
