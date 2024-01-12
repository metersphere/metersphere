package io.metersphere.functional.excel.constants;


import io.metersphere.functional.excel.domain.FunctionalCaseExcelData;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * @author wx
 */

public enum FunctionalCaseImportFiled {

    ID("id", "ID", "ID", "ID", FunctionalCaseExcelData::getNum),
    NAME("name", "用例名称", "用例名稱", "Name", FunctionalCaseExcelData::getName),
    MODULE("module", "所属模块", "所屬模塊", "Module", FunctionalCaseExcelData::getModule),
    TAGS("tags", "标签", "標簽", "Tag", FunctionalCaseImportFiled::parseTags),
    PREREQUISITE("prerequisite", "前置条件", "前置條件", "Prerequisite", FunctionalCaseExcelData::getPrerequisite),
    TEXT_DESCRIPTION("textDescription", "步骤描述", "步驟描述", "Text description", FunctionalCaseExcelData::getTextDescription),
    EXPECTED_RESULT("expectedResult", "预期结果", "預期結果", "Expected result", FunctionalCaseExcelData::getExpectedResult),
    CASE_EDIT_TYPE("caseEditType", "编辑模式", "編輯模式", "Case edit type", FunctionalCaseExcelData::getCaseEditType),
    DESCRIPTION("description", "备注", "備註", "Description", FunctionalCaseExcelData::getDescription);

    private Map<Locale, String> filedLangMap;
    private Function<FunctionalCaseExcelData, String> parseFunc;
    private String value;

    FunctionalCaseImportFiled(String value, String zn, String chineseTw, String us, Function<FunctionalCaseExcelData, String> parseFunc) {
        this.filedLangMap = new HashMap<Locale, String>();
        filedLangMap.put(Locale.SIMPLIFIED_CHINESE, zn);
        filedLangMap.put(Locale.TRADITIONAL_CHINESE, chineseTw);
        filedLangMap.put(Locale.US, us);
        this.value = value;
        this.parseFunc = parseFunc;
    }

    public Map<Locale, String> getFiledLangMap() {
        return this.filedLangMap;
    }

    public String getValue() {
        return value;
    }

    public String parseExcelDataValue(FunctionalCaseExcelData excelData) {
        return parseFunc.apply(excelData);
    }

    private static String parseTags(FunctionalCaseExcelData excelData) {
        String tags = StringUtils.EMPTY;
        try {
            if (excelData.getTags() != null) {
                List arr = JSON.parseArray(excelData.getTags());
                if (CollectionUtils.isNotEmpty(arr)) {
                    tags = StringUtils.joinWith(",", arr.toArray());
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return tags;
    }

    public boolean containsHead(String head) {
        return filedLangMap.values().contains(head);
    }
}
