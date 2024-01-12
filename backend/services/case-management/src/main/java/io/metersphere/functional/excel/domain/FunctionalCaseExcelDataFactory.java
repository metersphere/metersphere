package io.metersphere.functional.excel.domain;

import io.metersphere.system.excel.domain.ExcelDataFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * @author wx
 */
public class FunctionalCaseExcelDataFactory implements ExcelDataFactory {

    @Override
    public Class getExcelDataByLocal() {
        Locale locale = LocaleContextHolder.getLocale();
        if (Locale.US.toString().equalsIgnoreCase(locale.toString())) {
            return FunctionalCaseExcelDataUs.class;
        } else if (Locale.TRADITIONAL_CHINESE.toString().equalsIgnoreCase(locale.toString())) {
            return FunctionalCaseExcelDataTw.class;
        }
        return FunctionalCaseExcelDataCn.class;
    }

    public FunctionalCaseExcelData getFunctionalCaseExcelDataLocal() {
        Locale locale = LocaleContextHolder.getLocale();
        if (Locale.US.toString().equalsIgnoreCase(locale.toString())) {
            return new FunctionalCaseExcelDataUs();
        } else if (Locale.TRADITIONAL_CHINESE.toString().equalsIgnoreCase(locale.toString())) {
            return new FunctionalCaseExcelDataTw();
        }
        return new FunctionalCaseExcelDataCn();
    }

}
