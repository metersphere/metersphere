package io.metersphere.excel.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class IssueExcelDataFactory implements ExcelDataFactory{

    @Override
    public Class getExcelDataByLocal() {
        Locale locale = LocaleContextHolder.getLocale();
        if (StringUtils.equals(locale.toString(), Locale.US.toString())) {
            return IssueExcelDataUs.class;
        } else if (StringUtils.equals(locale.toString(), Locale.TRADITIONAL_CHINESE.toString())) {
            return IssueExcelDataTw.class;
        }
        return IssueExcelDataCn.class;
    }

    public IssueExcelData getIssueExcelDataLocal(){
        Locale locale = LocaleContextHolder.getLocale();
        if (StringUtils.equals(locale.toString(), Locale.US.toString())) {
            return new IssueExcelDataUs();
        } else if (StringUtils.equals(locale.toString(), Locale.TRADITIONAL_CHINESE.toString())) {
            return new IssueExcelDataTw();
        }
        return new IssueExcelDataCn();
    }
}
