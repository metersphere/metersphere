package io.metersphere.excel.constants;


import com.alibaba.nacos.common.utils.CollectionUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.excel.domain.TestCaseExcelData;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public enum TestCaseImportFiled {

    ID("id", "ID", "ID", "ID", TestCaseExcelData::getCustomNum),
    NAME("name", "用例名称", "用例名稱", "Name", TestCaseExcelData::getName),
    MODULE("module","所属模块", "所屬模塊", "Module", TestCaseExcelData::getNodePath),
    TAGS("tags","标签", "標簽", "Tag", TestCaseImportFiled::parseTags),
    PREREQUISITE("prerequisite","前置条件", "前置條件", "Prerequisite", TestCaseExcelData::getPrerequisite),
    STEP_DESC("stepDesc","步骤描述", "步驟描述", "Step description", TestCaseExcelData::getStepDesc),
    STEP_RESULT("stepResult","预期结果", "預期結果", "Step result", TestCaseExcelData::getStepResult),
    STEP_MODEL("stepModel","编辑模式", "編輯模式", "Edit Model", TestCaseExcelData::getStepModel),
    REMARK("remark","备注", "備註", "Remark", TestCaseExcelData::getRemark),
    STATUS("status","用例状态", "用例狀態", "Case status", TestCaseExcelData::getStatus),
    MAINTAINER("maintainer","责任人", "責任人", "Maintainer", TestCaseExcelData::getMaintainer),
    PRIORITY("priority","用例等级", "用例等級", "Priority", TestCaseExcelData::getPriority);

    private Map<Locale, String> filedLangMap;
    private Function<TestCaseExcelData, String> parseFunc;
    private String value;

    TestCaseImportFiled(String value, String zn, String chineseTw, String us, Function<TestCaseExcelData, String> parseFunc) {
        this.filedLangMap = new HashMap<>() {{
            put(Locale.SIMPLIFIED_CHINESE, zn);
            put(Locale.TRADITIONAL_CHINESE, chineseTw);
            put(Locale.US, us);
        }};
        this.value = value;
        this.parseFunc = parseFunc;
    }

    public Map<Locale, String> getFiledLangMap() {
        return this.filedLangMap;
    }

    public String getValue() {
        return value;
    }

    public String parseExcelDataValue(TestCaseExcelData excelData) {
        return parseFunc.apply(excelData);
    }

    private static String parseTags(TestCaseExcelData excelData) {
        String tags = StringUtils.EMPTY;
        try {
            if (excelData.getTags() != null) {
                List arr = JSON.parseArray(excelData.getTags());
                if (CollectionUtils.isNotEmpty(arr)) {
                    tags = StringUtils.joinWith(",", arr.toArray());
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return tags;
    }

    public boolean containsHead(String head) {
       return filedLangMap.values().contains(head);
    }
}
