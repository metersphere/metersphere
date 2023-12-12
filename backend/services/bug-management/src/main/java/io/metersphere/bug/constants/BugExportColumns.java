package io.metersphere.bug.constants;

import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.sdk.util.Translator;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 缺陷导出字段配置
 */
@Data
public class BugExportColumns {
    private LinkedHashMap<String, String> systemColumns = new LinkedHashMap<>();
    private LinkedHashMap<String, String> otherColumns = new LinkedHashMap<>();
    private LinkedHashMap<String, String> customColumns = new LinkedHashMap<>();

    public BugExportColumns() {
        systemColumns.put("name", Translator.get("bug.export.system.columns.name"));
        systemColumns.put("id", Translator.get("bug.export.system.columns.id"));
        systemColumns.put("content", Translator.get("bug.export.system.columns.content"));
        systemColumns.put("status", Translator.get("bug.export.system.columns.status"));
        systemColumns.put("handle_user", Translator.get("bug.export.system.columns.handle_user"));

        otherColumns.put("create_user", Translator.get("bug.export.system.other.columns.create_user"));
        otherColumns.put("create_time", Translator.get("bug.export.system.other.columns.create_time"));
        otherColumns.put("case_count", Translator.get("bug.export.system.other.columns.case_count"));
        otherColumns.put("comment", Translator.get("bug.export.system.other.columns.comment"));
        otherColumns.put("platform", Translator.get("bug.export.system.other.columns.platform"));

    }

    public void initCustomColumns(List<BugCustomFieldDTO> customFieldList) {
        customFieldList.forEach(item -> {
            customColumns.put(item.getId(), item.getName());
        });
    }
}
