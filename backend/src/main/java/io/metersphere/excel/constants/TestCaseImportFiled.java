package io.metersphere.excel.constants;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum TestCaseImportFiled {

    ID("ID", "ID", "ID"), NAME("用例名称", "用例名稱", "Name"), MODULE("所属模块", "所屬模塊", "Module"),
    TAG("标签", "標簽", "Tag"), PREREQUISITE("前置条件", "前置條件", "Prerequisite"), REMARK("备注", "備註", "Remark"),
    STEP_DESC("步骤描述", "步驟描述", "Step description"), STEP_RESULT("预期结果", "預期結果", "Step result"), STEP_MODEL("编辑模式", "編輯模式", "Edit Model"),
    STATUS("用例状态", "用例狀態", "Case status"), MAINTAINER("责任人", "責任人", "Maintainer"), PRIORITY("用例等级", "用例等級", "Priority");

    private Map<Locale, String> filedLangMap;

    public Map<Locale, String> getFiledLangMap() {
        return this.filedLangMap;
    }

    TestCaseImportFiled(String zn, String chineseTw, String us) {
        this.filedLangMap = new HashMap<>() {{
           put(Locale.SIMPLIFIED_CHINESE, zn);
           put(Locale.TRADITIONAL_CHINESE, chineseTw);
           put(Locale.US, us);
        }};
    }
}
