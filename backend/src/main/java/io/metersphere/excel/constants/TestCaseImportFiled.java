package io.metersphere.excel.constants;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.nacos.common.utils.CollectionUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.excel.domain.TestCaseExcelData;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public enum TestCaseImportFiled {

    ID("ID", "ID", "ID", TestCaseExcelData::getCustomNum),
    NAME("用例名称", "用例名稱", "Name", TestCaseExcelData::getName),
    MODULE("所属模块", "所屬模塊", "Module", TestCaseExcelData::getNodePath),
    TAG("标签", "標簽", "Tag", TestCaseImportFiled::parseTags),
    PREREQUISITE("前置条件", "前置條件", "Prerequisite", TestCaseExcelData::getPrerequisite),
    REMARK("备注", "備註", "Remark", TestCaseExcelData::getRemark),
    STEP_DESC("步骤描述", "步驟描述", "Step description", TestCaseExcelData::getStepDesc),
    STEP_RESULT("预期结果", "預期結果", "Step result", TestCaseExcelData::getStepResult),
    STEP_MODEL("编辑模式", "編輯模式", "Edit Model", TestCaseExcelData::getStepModel),
    STATUS("用例状态", "用例狀態", "Case status", TestCaseExcelData::getStatus),
    MAINTAINER("责任人", "責任人", "Maintainer", TestCaseExcelData::getMaintainer),
    PRIORITY("用例等级", "用例等級", "Priority", TestCaseExcelData::getPriority);

    private Map<Locale, String> filedLangMap;
    private Function<TestCaseExcelData, String> parseFunc;

    public Map<Locale, String> getFiledLangMap() {
        return this.filedLangMap;
    }

    TestCaseImportFiled(String zn, String chineseTw, String us, Function<TestCaseExcelData, String> parseFunc) {
        this.filedLangMap = new HashMap<>() {{
            put(Locale.SIMPLIFIED_CHINESE, zn);
            put(Locale.TRADITIONAL_CHINESE, chineseTw);
            put(Locale.US, us);
        }};
        this.parseFunc = parseFunc;
    }

    public String parseExcelDataValue(TestCaseExcelData excelData) {
        return parseFunc.apply(excelData);
    }

    private static String parseTags(TestCaseExcelData excelData) {
        String tags = "";
        try {
            if (excelData.getTags() != null) {
                JSONArray arr = JSONArray.parseArray(excelData.getTags());
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
