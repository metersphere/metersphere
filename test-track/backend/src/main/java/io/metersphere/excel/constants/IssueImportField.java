package io.metersphere.excel.constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum IssueImportField {

    /**
     * TITLE, DESCRIPTION, STATUS, PROCESSOR, PRIORITY
     */
    TITLE("title","缺陷标题", "缺陷標題", "Title"),
    DESCRIPTION("description","缺陷描述", "缺陷描述", "Description");
//    STATUS("status","状态", "狀態", "Status", IssueExcelData::getStatus),
//    PROCESSOR("processor","处理人", "處理人", "Processor", IssueExcelData::getProcessor),
//    PRIORITY("priority","严重程度", "嚴重程度", "Priority", IssueExcelData::getPriority);

    private Map<Locale, String> fieldLangMap;
    private String value;

    IssueImportField(String value, String zn, String chineseTw, String us) {
        this.fieldLangMap = new HashMap<>() {{
            put(Locale.SIMPLIFIED_CHINESE, zn);
            put(Locale.TRADITIONAL_CHINESE, chineseTw);
            put(Locale.US, us);
        }};
        this.value = value;
    }

    public Map<Locale, String> getFieldLangMap() {
        return this.fieldLangMap;
    }

    public String getValue() {
        return value;
    }

    public boolean containsHead(String head) {
        return this.fieldLangMap.containsValue(head);
    }
}
